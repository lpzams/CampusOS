package com.campus.api.controller.user;

import com.campus.api.auth.AuthInterceptor;
import com.campus.api.auth.Authenticated;
import com.campus.application.auth.AuthAppService;
import com.campus.common.api.Result;
import com.campus.common.exception.BusinessException;
import com.campus.domain.auth.CredentialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Authenticated
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final AuthAppService auth;
    private final Path uploadDir;
    public UserController(AuthAppService auth, @Value("${campus.upload-dir:uploads}") String uploadDir) {
        this.auth = auth;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/profile") public Result<Map<String, Object>> profile(HttpServletRequest request) { return Result.success(auth.profile(userId(request))); }
    @PutMapping("/profile") public Result<Map<String, Object>> update(HttpServletRequest request, @RequestBody Map<String, Object> body) { return Result.success(auth.updateProfile(userId(request), body)); }

    @PostMapping("/avatar")
    public Result<Map<String, Object>> avatar(HttpServletRequest request, @RequestParam MultipartFile file) throws Exception {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) throw new BusinessException(400, "头像不能为空且不能超过 5MB");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) throw new BusinessException(400, "只允许上传图片");
        String original = Path.of(file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename()).getFileName().toString();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.]", "") : "";
        String name = userId(request) + "-" + UUID.randomUUID() + extension;
        Path target = uploadDir.resolve("avatars").resolve(name).normalize();
        if (!target.startsWith(uploadDir)) throw new BusinessException(400, "文件名非法");
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String value = "/uploads/avatars/" + name;
        return Result.success(auth.updateProfile(userId(request), new LinkedHashMap<>(Map.of("avatar", value))));
    }

    @GetMapping("/profile/student") public Result<Map<String, Object>> student(HttpServletRequest request) { return Result.success(auth.studentProfile(userId(request))); }
    @GetMapping("/profile/teacher") public Result<Map<String, Object>> teacher(HttpServletRequest request) { return Result.success(auth.teacherProfile(userId(request))); }

    @PostMapping("/verify")
    public Result<Map<String, Object>> verify(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return Result.success(auth.verifyIdentity(userId(request), body));
    }

    @PutMapping("/password")
    public Result<Void> password(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        auth.changePassword(userId(request), String.valueOf(body.get("oldPassword")), String.valueOf(body.get("newPassword")));
        return Result.success();
    }

    private Long userId(HttpServletRequest request) {
        return ((CredentialService.TokenClaims) request.getAttribute(AuthInterceptor.PRINCIPAL)).userId();
    }
}
