package com.campus.api.controller.auth;

import com.campus.api.auth.Authenticated;
import com.campus.application.auth.AuthAppService;
import com.campus.common.api.Result;
import com.campus.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthAppService auth;
    private final boolean exposeSmsCode;

    public AuthController(AuthAppService auth, @Value("${campus.auth.expose-sms-code:false}") boolean exposeSmsCode) {
        this.auth = auth;
        this.exposeSmsCode = exposeSmsCode;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        return Result.success(auth.login(required(body, "username"), required(body, "password")));
    }

    @PostMapping("/login/sms")
    public Result<Map<String, Object>> loginSms(@RequestBody Map<String, Object> body) {
        return Result.success(auth.loginSms(required(body, "phone"), required(body, "code")));
    }

    @PostMapping("/sms/send")
    public Result<Map<String, Object>> sendSms(@RequestBody Map<String, Object> body) {
        String code = auth.sendSms(required(body, "phone"));
        return Result.success(exposeSmsCode ? Map.of("sent", true, "code", code) : Map.of("sent", true));
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/login/wechat")
    public Result<Map<String, Object>> loginWechat(@RequestBody Map<String, Object> body) {
        return Result.success(auth.loginWechat(required(body, "code"), (Map<String, Object>) body.get("userInfo")));
    }

    @PostMapping("/register") public Result<Map<String, Object>> register(@RequestBody Map<String, Object> body) { return Result.success(auth.register(body)); }
    @PostMapping("/forgot-password") public Result<Void> forgot(@RequestBody Map<String, Object> body) { auth.resetPassword(body); return Result.success(); }
    @Authenticated @PostMapping("/logout") public Result<Void> logout() { return Result.success(); }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestHeader("Authorization") String header) {
        if (!header.startsWith("Bearer ")) throw new BusinessException(401, "未授权");
        return Result.success(auth.refresh(header.substring(7)));
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) throw new BusinessException(400, key + " 不能为空");
        return value.toString();
    }
}
