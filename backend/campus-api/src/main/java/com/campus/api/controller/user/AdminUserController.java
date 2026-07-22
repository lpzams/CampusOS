package com.campus.api.controller.user;

import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.auth.AdminUserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import com.campus.common.exception.BusinessException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Equivalent of CampusOS_a administrator user-management endpoints. */
@Authenticated
@RoleAllowed({3})
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
    private final AdminUserAppService app;
    public AdminUserController(AdminUserAppService app) { this.app = app; }

    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) Integer userType,
            @RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(app.page(keyword, status, userType, pageNum, pageSize));
    }

    @PutMapping("/users/enable-all")
    public Result<Map<String, Integer>> enableAll() {
        return Result.success(Map.of("enabledCount", app.enableAllActiveAccounts()));
    }

    @PutMapping("/user/{userId}/audit")
    public Result<Map<String, Object>> audit(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        return Result.success(app.audit(userId, requiredInt(body, "status"), String.valueOf(body.getOrDefault("remark", ""))));
    }

    @PutMapping("/user/{userId}/status")
    public Result<Map<String, Object>> status(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        return Result.success(app.updateStatus(userId, requiredInt(body, "status"), String.valueOf(body.getOrDefault("remark", ""))));
    }

    @PutMapping("/user/{userId}/role")
    public Result<Map<String, Object>> role(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        return Result.success(app.updateRole(userId, requiredInt(body, "userType")));
    }

    @PutMapping("/user/{userId}/claim-crawler-teacher")
    public Result<Map<String, Object>> claimCrawlerTeacher(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Object password = body.get("newPassword");
        if (password == null) throw new BusinessException(400, "newPassword 不能为空");
        return Result.success(app.claimCrawlerTeacher(userId, String.valueOf(password)));
    }

    @DeleteMapping("/user/{userId}")
    public Result<Void> delete(@PathVariable Long userId) { app.deactivate(userId); return Result.success(); }

    private int requiredInt(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (!(value instanceof Number number)) throw new BusinessException(400, key + " 必须为数字");
        return number.intValue();
    }
}
