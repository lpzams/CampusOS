package com.campus.application.user.service;

import com.campus.application.user.command.ForgotPasswordCommand;
import com.campus.application.user.command.LoginCommand;
import com.campus.application.user.command.RegisterCommand;
import com.campus.application.user.command.SmsLoginCommand;
import com.campus.application.user.command.WechatLoginCommand;
import com.campus.application.user.dto.LoginResponseDTO;
import com.campus.common.api.ResultCode;
import com.campus.common.constant.RedisConstants;
import com.campus.common.constant.UserConstants;
import com.campus.common.context.LoginUser;
import com.campus.common.context.LoginUserHolder;
import com.campus.common.exception.BusinessException;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;
import com.campus.domain.user.repository.UserRepository;
import com.campus.infrastructure.util.JwtUtil;
import com.campus.infrastructure.util.PasswordEncoder;
import com.campus.infrastructure.util.WechatApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WechatApiClient wechatApiClient;

    // ==================== 注册 ====================

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterCommand command) {
        log.info("用户注册开始: username={}, phone={}", command.getUsername(), command.getPhone());

        // 1. 校验验证码（查 Redis）
        log.info("校验验证码: phone={}, smsCode={}", command.getPhone(), command.getSmsCode());
        smsService.validateSmsCode(command.getPhone(), command.getSmsCode());

        // 2. 用户名是否已存在
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 3. 手机号是否已注册
        if (userRepository.existsByPhone(command.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        // 4. 邮箱是否已被使用
        if (command.getEmail() != null && !command.getEmail().isEmpty()
                && userRepository.existsByEmail(command.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 5. 创建用户
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        User user = User.createForRegister(
                command.getUsername(),
                encodedPassword,
                command.getRealName(),
                command.getPhone(),
                command.getEmail(),
                command.getUserType(),
                command.getDepartment()
        );

        // 6. 创建 Profile
        UserProfile profile = null;
        if (user.getUserType() == UserConstants.TYPE_STUDENT) {
            profile = UserProfile.createStudent(
                    null,
                    command.getMajor(),
                    command.getClassName(),
                    command.getEnrollmentYear()
            );
        }
        if (user.getUserType() == UserConstants.TYPE_TEACHER) {
            profile = UserProfile.createTeacher(
                    null,
                    command.getTitle(),
                    command.getResearchDirection()
            );
        }

        // 7. 一次性保存
        userRepository.save(user, profile, null);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    // ==================== 用户名密码登录 ====================

    public LoginResponseDTO loginByPassword(LoginCommand command) {
        log.info("用户名密码登录: username={}", command.getUsername());

        // 1. 检查登录失败次数（Redis）
        checkLoginFailLimit(command.getUsername());

        // 2. 查询用户
        User user = userRepository.findByUsername(command.getUsername());
        if (user == null) {
            recordLoginFail(command.getUsername());
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 3. 检查账号状态（冻结/待审核/注销）
        user.checkStatus();

        // 4. 检查锁定状态（5次失败锁定30分钟 - DB层面）
        user.checkLock();

        // 5. 校验密码
        if (!passwordEncoder.matches(command.getPassword(), user.getPasswordHash())) {
            user.loginFailed();
            userRepository.update(user);
            recordLoginFail(command.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 6. 登录成功 — 更新登录信息
        user.loginSuccess(null);
        userRepository.update(user);

        // 7. 清除 Redis 失败计数
        clearLoginFail(command.getUsername());

        // 8. 生成 JWT 并存入 Redis，返回响应
        return buildLoginResponse(user);
    }

    // ==================== 手机验证码登录 ====================

    public LoginResponseDTO loginBySms(SmsLoginCommand command) {
        log.info("手机验证码登录: phone={}", command.getPhone());

        // 1. 校验短信验证码
        smsService.validateSmsCode(command.getPhone(), command.getCode());

        // 2. 根据手机号查询用户
        User user = userRepository.findByPhone(command.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 3. 检查账号状态
        user.checkStatus();

        // 4. 检查锁定状态
        user.checkLock();

        // 5. 登录成功
        user.loginSuccess(null);
        userRepository.update(user);

        // 6. 生成 JWT 并存入 Redis，返回响应
        return buildLoginResponse(user);
    }

    // ==================== 微信授权登录 ====================

    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO loginByWechat(WechatLoginCommand command) {
        log.info("微信授权登录: code={}", command.getCode().substring(0, Math.min(10, command.getCode().length())) + "...");

        // 1. 用 code 换取微信 openId
        WechatApiClient.WechatSession session = wechatApiClient.code2Session(command.getCode());
        String openId = session.getOpenId();
        log.info("微信 openId: {}", openId.substring(0, Math.min(10, openId.length())) + "...");

        // 2. 根据 openId 查询用户
        User user = userRepository.findByOpenId(openId);
        boolean isFirstLogin = (user == null);

        if (isFirstLogin) {
            // 3a. 首次登录 → 自动注册
            String nickName = null;
            String avatarUrl = null;
            if (command.getUserInfo() != null) {
                nickName = command.getUserInfo().getNickName();
                avatarUrl = command.getUserInfo().getAvatarUrl();
            }
            user = User.createForWechat(openId, nickName, avatarUrl);

            // 保存用户（含默认角色）
            UserRole role = UserRole.createDefaultRole(null, user.getUserType());
            userRepository.save(user, null, role);
            log.info("微信新用户自动注册: userId={}, openId={}", user.getId(), openId.substring(0, Math.min(10, openId.length())) + "...");
        } else {
            // 3b. 老用户 → 检查账号状态
            user.checkStatus();
            user.checkLock();
        }

        // 4. 登录成功 — 更新登录信息，同步微信头像/昵称
        if (command.getUserInfo() != null) {
            if (command.getUserInfo().getAvatarUrl() != null) {
                user.setAvatar(command.getUserInfo().getAvatarUrl());
            }
            if (command.getUserInfo().getNickName() != null) {
                user.setRealName(command.getUserInfo().getNickName());
            }
        }
        user.loginSuccess(null);
        userRepository.update(user);

        // 5. 生成 JWT + Redis → 返回响应
        LoginResponseDTO response = buildLoginResponse(user);
        response.setIsFirstLogin(isFirstLogin);
        return response;
    }

    // ==================== 退出登录 ====================

    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        String tokenKey = RedisConstants.getTokenKey(token);
        Boolean deleted = stringRedisTemplate.delete(tokenKey)
                && stringRedisTemplate.delete(RedisConstants.getUserInfoKey(LoginUserHolder.get().getUserId()));
        log.info("退出登录: token={}, deleted={}", token.substring(0, Math.min(16, token.length())) + "...", deleted);
    }

    // ==================== 忘记密码 ====================

    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordCommand command) {
        log.info("忘记密码: username={}, phone={}", command.getUsername(), command.getPhone());

        // 1. 校验短信验证码
        smsService.validateSmsCode(command.getPhone(), command.getCode());

        // 2. 根据用户名查询用户
        User user = userRepository.findByUsername(command.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 3. 校验手机号是否与注册时一致
        user.validatePhoneMatch(command.getPhone());

        // 4. 检查账号状态（冻结/注销等）
        user.checkStatus();

        // 5. 检查锁定状态
        user.checkLock();

        // 6. 加密新密码并重置
        String encodedPassword = passwordEncoder.encode(command.getNewPassword());
        user.resetPassword(encodedPassword);
        userRepository.update(user);

        // 7. 清除该用户现有的 Token（强制重新登录）
        clearUserTokens(user.getId());

        // 8. 清除登录失败计数
        clearLoginFail(command.getUsername());

        log.info("密码重置成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查 Redis 中的登录失败次数
     */
    private void checkLoginFailLimit(String username) {
        String failKey = RedisConstants.getLoginFailKey(username);
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        if (failCountStr != null) {
            int failCount = Integer.parseInt(failCountStr);
            if (failCount >= RedisConstants.LOGIN_FAIL_MAX_COUNT) {
                throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
            }
        }
    }

    /**
     * 记录登录失败（Redis 自增）
     */
    private void recordLoginFail(String username) {
        String failKey = RedisConstants.getLoginFailKey(username);
        stringRedisTemplate.opsForValue().increment(failKey);
        stringRedisTemplate.expire(failKey, RedisConstants.LOGIN_FAIL_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 清除登录失败计数
     */
    private void clearLoginFail(String username) {
        String failKey = RedisConstants.getLoginFailKey(username);
        stringRedisTemplate.delete(failKey);
    }

    /**
     * 清除用户所有 Token（密码重置、强制下线等场景）
     */
    private void clearUserTokens(Long userId) {
        String userTokenKey = RedisConstants.getUserTokenKey(userId);
        String existingToken = stringRedisTemplate.opsForValue().get(userTokenKey);
        if (existingToken != null) {
            stringRedisTemplate.delete(RedisConstants.getTokenKey(existingToken));
            stringRedisTemplate.delete(userTokenKey);
            log.info("已清除用户 Token: userId={}", userId);
        }
    }

    /**
     * 构建登录响应：生成 JWT → 存入 Redis → 组装 DTO
     */
    private LoginResponseDTO buildLoginResponse(User user) {
        String tokenKey = RedisConstants.getUserTokenKey(user.getId());

        // 1. 先检查 Redis 中是否存在该用户的 token
        String existingToken = stringRedisTemplate.opsForValue().get(tokenKey);

        String token;
        if (existingToken != null) {
            // 存在：复用已有 token，刷新过期时间
            token = existingToken;
            String tokenKeyWithPrefix = RedisConstants.getTokenKey(token);
            stringRedisTemplate.expire(tokenKeyWithPrefix, RedisConstants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.expire(tokenKey, RedisConstants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("Token 已存在，刷新过期时间: userId={}, token={}", user.getId(), token);
        } else {
            // 不存在：生成新 token
            token = jwtUtil.generateToken(user.getId(), user.getUsername());
            log.info("生成新 Token: userId={}, token={}", user.getId(), token);
        }

        // 2. 构建 LoginUser 并序列化为 JSON 存入 Redis
        LoginUser loginUser = LoginUser.of(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getUserType(),
                user.getAvatar()
        );

        try {
            String userJson = objectMapper.writeValueAsString(loginUser);

            // 存储 token → user 的映射
            String tokenKeyWithPrefix = RedisConstants.getTokenKey(token);
            stringRedisTemplate.opsForValue().set(
                    tokenKeyWithPrefix,
                    userJson,
                    RedisConstants.TOKEN_EXPIRE_HOURS,
                    TimeUnit.HOURS
            );

            // 存储 user → token 的映射（用于快速查找用户当前 token）
            stringRedisTemplate.opsForValue().set(
                    tokenKey,
                    token,
                    RedisConstants.TOKEN_EXPIRE_HOURS,
                    TimeUnit.HOURS
            );

        } catch (JsonProcessingException e) {
            log.error("序列化 LoginUser 失败", e);
            throw new BusinessException("系统异常，登录失败");
        }

        // 3. 组装响应
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .avatar(user.getAvatar())
                .expiresIn(RedisConstants.TOKEN_EXPIRE_HOURS * 1L)
                .build();
    }
}
