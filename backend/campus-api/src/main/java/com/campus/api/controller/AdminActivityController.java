package com.campus.api.controller;

import com.campus.application.activity.service.ActivityAppService;
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
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "管理员活动CRUD、报名列表相关接口")
public class AdminActivityController {

    private final ActivityAppService activityAppService;

    // ==================== 13.7 创建活动（管理员） ====================

    @PostMapping
    @Operation(summary = "创建活动")
    public Result<Map<String, Object>> createActivity(@RequestBody Map<String, Object> command) {
        log.info("创建活动: title={}", command.get("title"));
        return Result.success(activityAppService.createActivity(command));
    }

    // ==================== 13.8 更新活动（管理员） ====================

    @PutMapping("/{id}")
    @Operation(summary = "更新活动")
    public Result<Map<String, Object>> updateActivity(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> command) {
        log.info("更新活动: id={}", id);
        return Result.success(activityAppService.updateActivity(id, command));
    }

    // ==================== 13.9 删除活动（管理员） ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        log.info("删除活动: activityId={}", id);
        activityAppService.deleteActivity(id);
        return Result.success();
    }

    // ==================== 13.10 获取活动列表（管理员） ====================

    @GetMapping("/list")
    @Operation(summary = "获取活动列表（管理员）")
    public Result<PageResult<Map<String, Object>>> getAdminActivityList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("管理员活动列表: category={}, status={}, page={}", category, status, page);
        return Result.success(activityAppService.getAdminActivityList(category, status, page, size));
    }

    // ==================== 13.11 活动报名列表（管理员） ====================

    @GetMapping("/{id}/registrations")
    @Operation(summary = "活动报名列表")
    public Result<PageResult<Map<String, Object>>> getActivityRegistrations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("活动报名列表: activityId={}, page={}", id, page);
        return Result.success(activityAppService.getActivityRegistrations(id, page, size));
    }
}
