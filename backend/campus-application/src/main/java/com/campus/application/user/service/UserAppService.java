package com.campus.application.user.service;

import com.campus.application.user.command.RegisterCommand;
import com.campus.common.api.ResultCode;
import com.campus.common.constant.UserConstants;
import com.campus.common.exception.BusinessException;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;
import com.campus.domain.user.repository.UserRepository;
import com.campus.infrastructure.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;

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
        // 1. 创建 User（不含 id）
        User user = User.createForRegister(
                command.getUsername(),
                encodedPassword,
                command.getRealName(),
                command.getPhone(),
                command.getEmail(),
                command.getUserType(),
                command.getDepartment()
        );

        // 2. 创建 Profile（userId 传 null，Repository 会回填）
        UserProfile profile = null;
        if (user.getUserType() == UserConstants.TYPE_STUDENT) {
            profile = UserProfile.createStudent(
                    null,  // ← 传 null，Repository 会设置
                    command.getMajor(),
                    command.getClassName(),
                    command.getEnrollmentYear()
            );
        }
        if (user.getUserType() == UserConstants.TYPE_TEACHER) {
            profile = UserProfile.createTeacher(
                    null,  // ← 传 null，Repository 会设置
                    command.getTitle(),
                    command.getResearchDirection()
            );
        }// ← 传 null

        // 4. 一次性保存，Repository 内部处理 userId 关联
        userRepository.save(user, profile, null);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }
}