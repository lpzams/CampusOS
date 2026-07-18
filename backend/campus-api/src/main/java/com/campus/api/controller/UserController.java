package com.campus.api.controller;

import com.campus.application.user.command.ForgotPasswordCommand;
import com.campus.application.user.command.LoginCommand;
import com.campus.application.user.command.RegisterCommand;
import com.campus.application.user.command.SmsLoginCommand;
import com.campus.application.user.command.WechatLoginCommand;
import com.campus.application.user.dto.LoginResponseDTO;
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
@Tag(name = "用户认证", description = "登录、注册相关接口")
public class UserController {

    private final UserAppService userAppService;
    private final SmsService smsService;

    // ==================== 登录 ====================

    @PostMapping("/login")
    @Operation(summary = "账号密码登录", description = "使用用户名和密码登录")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginCommand command) {
        log.info("账号密码登录: username={}", command.getUsername());
        LoginResponseDTO response = userAppService.loginByPassword(command);
        return Result.success(response);
    }

    @PostMapping("/login/sms")
    @Operation(summary = "手机验证码登录", description = "使用手机号和验证码登录")
    public Result<LoginResponseDTO> loginBySms(@Valid @RequestBody SmsLoginCommand command) {
        log.info("手机验证码登录: phone={}", command.getPhone());
        LoginResponseDTO response = userAppService.loginBySms(command);
        return Result.success(response);
    }

    @PostMapping("/login/wechat")
    @Operation(summary = "微信授权登录", description = "使用微信授权code登录，首次登录自动注册")
    public Result<LoginResponseDTO> loginByWechat(@Valid @RequestBody WechatLoginCommand command) {
        log.info("微信授权登录: code={}", command.getCode() != null ? command.getCode().substring(0, Math.min(10, command.getCode().length())) + "..." : "null");
        LoginResponseDTO response = userAppService.loginByWechat(command);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "清除Token，退出登录")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        log.info("退出登录: token={}", token != null ? token.substring(0, Math.min(16, token.length())) + "..." : "null");
        userAppService.logout(token);
        return Result.success();
    }

    // ==================== 注册 ====================

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "学生/教师注册账号")
    public Result<Void> register(@Valid @RequestBody RegisterCommand command) {
        log.info("用户注册: {}", command);
        userAppService.register(command);
        return Result.success();
    }

    // ==================== 忘记密码 ====================

    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "通过手机验证码重置密码，重置后需重新登录")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordCommand command) {
        log.info("忘记密码: username={}, phone={}", command.getUsername(), command.getPhone());
        userAppService.forgotPassword(command);
        return Result.success();
    }

    // ==================== 短信验证码 ====================

    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码", description = "发送短信验证码")
    public Result<Void> sendSmsCode(@RequestParam @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        log.info("发送短信验证码: phone={}", phone);
        smsService.sendSmsCode(phone);
        return Result.success();
    }

    // ==================== 私有方法 ====================

    /**
     * 从 Authorization 请求头中提取 Token
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
