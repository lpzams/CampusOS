package com.campus.api.controller;

import com.campus.application.user.command.*;
import com.campus.application.user.dto.*;
import com.campus.application.user.service.SmsService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "登录、注册、个人信息相关接口")
public class UserController {

    private final UserAppService userAppService;
    private final SmsService smsService;

    // ==================== 登录 ====================

    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginCommand command) {
        log.info("账号密码登录: username={}", command.getUsername());
        return Result.success(userAppService.loginByPassword(command));
    }

    @PostMapping("/login/sms")
    @Operation(summary = "手机验证码登录")
    public Result<LoginResponseDTO> loginBySms(@Valid @RequestBody SmsLoginCommand command) {
        log.info("手机验证码登录: phone={}", command.getPhone());
        return Result.success(userAppService.loginBySms(command));
    }

    @PostMapping("/login/wechat")
    @Operation(summary = "微信授权登录")
    public Result<LoginResponseDTO> loginByWechat(@Valid @RequestBody WechatLoginCommand command) {
        log.info("微信授权登录");
        return Result.success(userAppService.loginByWechat(command));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        userAppService.logout(extractToken(authHeader));
        return Result.success();
    }

    // ==================== 注册 ====================

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterCommand command) {
        userAppService.register(command);
        return Result.success();
    }

    // ==================== 忘记密码 ====================

    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordCommand command) {
        log.info("忘记密码: username={}", command.getUsername());
        userAppService.forgotPassword(command);
        return Result.success();
    }

    // ==================== 短信验证码 ====================

    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码")
    public Result<Void> sendSmsCode(@RequestParam @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        smsService.sendSmsCode(phone);
        return Result.success();
    }

    // ==================== 2.1 获取个人信息 ====================

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public Result<UserProfileDTO> getProfile() {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.getProfile(userId));
    }

    // ==================== 2.2 修改个人信息（含头像） ====================

    @PutMapping("/profile")
    @Operation(summary = "修改个人信息", description = "支持修改真实姓名、手机号、邮箱、性别、头像URL")
    public Result<UserProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.updateProfile(userId, command));
    }

    // ==================== 2.4 获取学生详细信息 ====================

    @GetMapping("/profile/student")
    @Operation(summary = "获取学生详细信息")
    public Result<StudentDetailDTO> getStudentDetail() {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.getStudentDetail(userId));
    }

    // ==================== 2.5 获取教师详细信息 ====================

    @GetMapping("/profile/teacher")
    @Operation(summary = "获取教师详细信息")
    public Result<TeacherDetailDTO> getTeacherDetail() {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.getTeacherDetail(userId));
    }

    // ==================== 2.8 修改学生详细信息 ====================

    @PutMapping("/profile/student")
    @Operation(summary = "修改学生详细信息")
    public Result<StudentProfileDTO> updateStudentProfile(@RequestBody UpdateStudentProfileCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.updateStudentProfile(userId, command));
    }

    // ==================== 2.9 修改教师详细信息 ====================

    @PutMapping("/profile/teacher")
    @Operation(summary = "修改教师详细信息")
    public Result<TeacherProfileDTO> updateTeacherProfile(@RequestBody UpdateTeacherProfileCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.updateTeacherProfile(userId, command));
    }

    // ==================== 2.6 实名认证 ====================

    @PostMapping("/verify")
    @Operation(summary = "实名认证")
    public Result<VerifyResponseDTO> verify(@Valid @RequestBody VerifyCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        return Result.success(userAppService.submitVerify(userId, command));
    }

    // ==================== 2.7 修改密码 ====================

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        userAppService.changePassword(userId, command);
        return Result.success();
    }

    // ==================== 私有方法 ====================

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
