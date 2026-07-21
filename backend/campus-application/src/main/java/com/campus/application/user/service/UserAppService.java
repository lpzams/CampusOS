package com.campus.application.user.service;

import com.campus.application.user.command.*;
import com.campus.application.user.dto.*;
import com.campus.common.api.ResultCode;
import com.campus.common.constant.RedisConstants;
import com.campus.common.constant.UserConstants;
import com.campus.common.context.LoginUser;
import com.campus.common.context.LoginUserHolder;
import com.campus.common.exception.BusinessException;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;
import com.campus.domain.user.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;

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
                    command.getUsername(),
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

    // ==================== 2.1 获取个人信息 ====================

    public UserProfileDTO getProfile(Long userId) {
        // 1. 查 User（Cache-Aside）
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 查 Profile（Cache-Aside）
        UserProfile profile = userProfileRepository.findByUserId(userId);

        // 3. 拼装返回
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .gender(genderToString(user.getGender()))
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .department(user.getDepartment())
                .major(profile != null ? profile.getMajor() : null)
                .className(profile != null ? profile.getClassName() : null)
                .studentId(profile != null ? profile.getStudentId() : null)
                .enrollmentYear(profile != null ? profile.getEnrollmentYear() : null)
                .status(user.getStatus())
                .createdTime(user.getCreatedTime() != null ? user.getCreatedTime().toString() : null)
                .build();
    }

    // ==================== 2.2 修改个人信息（含头像） ====================

    @Transactional(rollbackFor = Exception.class)
    public UserProfileDTO updateProfile(Long userId, UpdateProfileCommand command) {
        // 1. 查用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 手机号唯一性校验（如果修改了手机号）
        if (command.getPhone() != null && !command.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(command.getPhone())) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }
            user.setPhone(command.getPhone());
        }

        // 3. 邮箱唯一性校验（如果修改了邮箱）
        if (command.getEmail() != null && !command.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(command.getEmail())) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
            user.setEmail(command.getEmail());
        }

        // 4. 更新基本信息（含头像）
        if (command.getRealName() != null) {
            user.setRealName(command.getRealName());
        }
        if (command.getGender() != null) {
            user.setGender(command.getGender());
        }
        if (command.getAvatar() != null) {
            user.setAvatar(command.getAvatar());
        }

        // 5. 写 DB → 自动删缓存（Cache-Aside Write）
        userRepository.update(user);

        // 6. 查 Profile 拼完整响应
        UserProfile profile = userProfileRepository.findByUserId(userId);

        log.info("个人信息修改成功: userId={}", userId);
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .gender(genderToString(user.getGender()))
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .department(user.getDepartment())
                .major(profile != null ? profile.getMajor() : null)
                .className(profile != null ? profile.getClassName() : null)
                .studentId(profile != null ? profile.getStudentId() : null)
                .enrollmentYear(profile != null ? profile.getEnrollmentYear() : null)
                .status(user.getStatus())
                .createdTime(user.getCreatedTime() != null ? user.getCreatedTime().toString() : null)
                .updatedTime(user.getUpdatedTime() != null ? user.getUpdatedTime().toString() : null)
                .build();
    }

    // ==================== 2.4 获取学生详细信息 ====================

    public StudentDetailDTO getStudentDetail(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!user.isStudent()) {
            throw new BusinessException("仅学生可访问此接口");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId);

        return StudentDetailDTO.builder()
                .userId(user.getId())
                .studentId(profile != null ? profile.getStudentId() : null)
                .realName(user.getRealName())
                .gender(genderToString(user.getGender()))
                .department(user.getDepartment())
                .major(profile != null ? profile.getMajor() : null)
                .className(profile != null ? profile.getClassName() : null)
                .enrollmentYear(profile != null ? profile.getEnrollmentYear() : null)
                .dormitory(profile != null ? profile.getDormitory() : null)
                .advisor(profile != null ? profile.getAdvisor() : null)
                .build();
    }

    // ==================== 2.5 获取教师详细信息 ====================

    public TeacherDetailDTO getTeacherDetail(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!user.isTeacher()) {
            throw new BusinessException("仅教师可访问此接口");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId);

        return TeacherDetailDTO.builder()
                .userId(user.getId())
                .teacherId(profile != null ? profile.getTeacherId() : null)
                .realName(user.getRealName())
                .gender(genderToString(user.getGender()))
                .department(user.getDepartment())
                .title(profile != null ? profile.getTitle() : null)
                .office(profile != null ? profile.getOffice() : null)
                .phone(user.getPhone())
                .email(user.getEmail())
                .researchArea(profile != null ? profile.getResearchDirection() : null)
                .introduction(profile != null ? profile.getIntroduction() : null)
                .build();
    }

    // ==================== 2.8 修改学生详细信息 ====================

    @Transactional(rollbackFor = Exception.class)
    public StudentProfileDTO updateStudentProfile(Long userId, UpdateStudentProfileCommand command) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!user.isStudent()) {
            throw new BusinessException("仅学生可修改此信息");
        }

        // 获取或创建 Profile
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        // Domain 层更新
        profile.updateStudentInfo(command.getMajor(), command.getClassName(),
                command.getEnrollmentYear(), command.getDormitory(), command.getAdvisor());

        if (profile.getId() == null) {
            userProfileRepository.save(profile);
        } else {
            userProfileRepository.update(profile);
        }

        log.info("学生信息修改成功: userId={}", userId);

        return StudentProfileDTO.builder()
                .userId(userId)
                .studentId(profile.getStudentId())
                .major(profile.getMajor())
                .className(profile.getClassName())
                .enrollmentYear(profile.getEnrollmentYear())
                .dormitory(profile.getDormitory())
                .advisor(profile.getAdvisor())
                .build();
    }

    // ==================== 2.9 修改教师详细信息 ====================

    @Transactional(rollbackFor = Exception.class)
    public TeacherProfileDTO updateTeacherProfile(Long userId, UpdateTeacherProfileCommand command) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!user.isTeacher()) {
            throw new BusinessException("仅教师可修改此信息");
        }

        // 获取或创建 Profile
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        // Domain 层更新
        profile.updateTeacherInfo(command.getTitle(), command.getResearchDirection(),
                command.getOffice(), command.getIntroduction());

        if (profile.getId() == null) {
            userProfileRepository.save(profile);
        } else {
            userProfileRepository.update(profile);
        }

        log.info("教师信息修改成功: userId={}", userId);

        return TeacherProfileDTO.builder()
                .userId(userId)
                .teacherId(profile.getTeacherId())
                .title(profile.getTitle())
                .researchDirection(profile.getResearchDirection())
                .office(profile.getOffice())
                .introduction(profile.getIntroduction())
                .build();
    }

    // ==================== 2.6 实名认证 ====================

    @Transactional(rollbackFor = Exception.class)
    public VerifyResponseDTO submitVerify(Long userId, VerifyCommand command) {
        // 1. 查 User
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 获取或创建 Profile
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        // 3. 提交认证（Domain 层校验状态）
        profile.submitVerify(command.getRealName(), command.getIdCard(),
                command.getIdCardFront(), command.getIdCardBack(),
                profile.getStudentId());

        // 4. 保存 → 自动删缓存
        if (profile.getId() == null) {
            userProfileRepository.save(profile);
        } else {
            userProfileRepository.update(profile);
        }

        // 5. 同步更新 User 实名认证标识
        user.setIsVerified(1);  // 审核中
        user.setRealName(command.getRealName());
        userRepository.update(user);

        log.info("实名认证提交成功: userId={}, realName={}", userId, command.getRealName());

        // 6. 返回
        String expectTime = java.time.LocalDateTime.now().plusDays(3)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return VerifyResponseDTO.builder()
                .verifyId(profile.getId())
                .status("PENDING")
                .statusDesc("审核中")
                .submitTime(profile.getUpdatedTime() != null ? profile.getUpdatedTime().toString() : null)
                .expectedFinishTime(expectTime)
                .build();
    }

    // ==================== 2.7 修改密码 ====================

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordCommand command) {
        // 1. 查用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 校验旧密码
        if (user.getPasswordHash() == null) {
            throw new BusinessException("当前账号无密码，无法修改（请使用微信登录后设置密码）");
        }
        if (!passwordEncoder.matches(command.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 3. 新密码不能与旧密码相同
        if (passwordEncoder.matches(command.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.NEW_PASSWORD_SAME);
        }

        // 4. 更新密码
        String encodedPassword = passwordEncoder.encode(command.getNewPassword());
        user.resetPassword(encodedPassword);
        userRepository.update(user);

        // 5. 清除该用户所有 Token（强制重新登录）
        clearUserTokens(userId);

        log.info("密码修改成功: userId={}", userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从 ThreadLocal 获取当前登录用户 ID
     */
    public static Long getCurrentUserId() {
        LoginUser loginUser = LoginUserHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser.getUserId();
    }

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
     * 性别 int → 中文
     */
    private String genderToString(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
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
     * <p>
     * 每次登录都生成全新 JWT，旧的 user→token 映射自动清除。
     * JWT 内部 exp 不可变，不能复用旧 token 仅刷新 Redis TTL。
     */
    private LoginResponseDTO buildLoginResponse(User user) {
        String userTokenKey = RedisConstants.getUserTokenKey(user.getId());

        // 1. 清除旧的 user→token 映射（如果存在），防止旧 token 残留
        String oldToken = stringRedisTemplate.opsForValue().get(userTokenKey);
        if (oldToken != null) {
            stringRedisTemplate.delete(RedisConstants.getTokenKey(oldToken));
            log.debug("清除旧 Token: userId={}", user.getId());
        }

        // 2. 生成全新 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("生成新 Token: userId={}, token={}", user.getId(), token);

        // 3. 构建 LoginUser 并序列化为 JSON 存入 Redis
        LoginUser loginUser = LoginUser.of(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getUserType(),
                user.getAvatar()
        );

        try {
            String userJson = objectMapper.writeValueAsString(loginUser);

            // 存储 token → user 的映射（AuthInterceptor 校验用）
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.getTokenKey(token),
                    userJson,
                    RedisConstants.TOKEN_EXPIRE_HOURS,
                    TimeUnit.HOURS
            );

            // 存储 user → token 的映射（用于登出/强制下线等场景）
            stringRedisTemplate.opsForValue().set(
                    userTokenKey,
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
