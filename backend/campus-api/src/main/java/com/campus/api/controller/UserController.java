package com.campus.api.controller;

import com.campus.application.user.command.RegisterCommand;
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

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "学生/教师注册账号")
    public Result<Void> register(@Valid @RequestBody RegisterCommand command) {
        log.info("用户注册: {}", command);
        userAppService.register(command);
        return Result.success();
    }

    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码", description = "发送短信验证码")
    public Result<Void> sendSmsCode(@RequestParam @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        log.info("发送短信验证码: phone={}", phone);
        smsService.sendSmsCode(phone);
        return Result.success();
    }
}