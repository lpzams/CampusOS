package com.campus.api.controller;

import com.campus.application.user.service.AdminUserService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "管理员用户列表、审核、启用/禁用、删除相关接口")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ==================== 1.8 获取用户列表（管理员） ====================

    @GetMapping("/users")
    @Operation(summary = "获取用户列表（管理员）")
    public Result<PageResult<Map<String, Object>>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer userType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("管理员用户列表: keyword={}, status={}, userType={}, page={}", keyword, status, userType, page);
        return Result.success(adminUserService.getUserList(keyword, status, userType, page, size));
    }

    // ==================== 1.9 审核用户（管理员） ====================

    @PutMapping("/user/{userId}/audit")
    @Operation(summary = "审核用户（管理员）")
    public Result<Map<String, Object>> auditUser(@PathVariable Long userId,
                                                  @RequestBody Map<String, Object> command) {
        log.info("审核用户: userId={}, status={}", userId, command.get("status"));
        return Result.success(adminUserService.auditUser(userId, command));
    }

    // ==================== 1.10 启用/禁用用户（管理员） ====================

    @PutMapping("/user/{userId}/status")
    @Operation(summary = "启用/禁用用户（管理员）")
    public Result<Map<String, Object>> updateUserStatus(@PathVariable Long userId,
                                                         @RequestBody Map<String, Object> command) {
        log.info("启用/禁用用户: userId={}, status={}", userId, command.get("status"));
        return Result.success(adminUserService.updateUserStatus(userId, command));
    }

    // ==================== 1.11 删除用户（管理员） ====================

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "删除用户（管理员）")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        log.info("管理员删除用户: userId={}", userId);
        adminUserService.deleteUser(userId);
        return Result.success();
    }
}
