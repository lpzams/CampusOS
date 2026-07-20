package com.campus.api.controller;

import com.campus.application.activity.service.ActivityAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "校园活动", description = "活动列表、详情、报名、取消、签到、我的活动相关接口")
public class ActivityController {

    private final ActivityAppService activityAppService;

    // ==================== 13.1 获取活动列表 ====================

    @GetMapping("/list")
    @Operation(summary = "获取活动列表")
    public Result<PageResult<Map<String, Object>>> getActivityList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = null;
        try {
            currentUserId = UserAppService.getCurrentUserId();
        } catch (Exception ignored) {
            // 未登录用户也可以浏览
        }
        log.info("获取活动列表: category={}, status={}, page={}", category, status, page);
        return Result.success(activityAppService.getActivityList(category, status, page, size, currentUserId));
    }

    // ==================== 13.2 获取活动详情 ====================

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取活动详情")
    public Result<Map<String, Object>> getActivityDetail(@PathVariable Long id) {
        Long currentUserId = null;
        try {
            currentUserId = UserAppService.getCurrentUserId();
        } catch (Exception ignored) {
            // 未登录用户也可以浏览详情
        }
        log.info("获取活动详情: id={}, userId={}", id, currentUserId);
        return Result.success(activityAppService.getActivityDetail(id, currentUserId));
    }

    // ==================== 13.3 报名活动 ====================

    @PostMapping("/register")
    @Operation(summary = "报名活动")
    public Result<Map<String, Object>> registerActivity(@RequestBody Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("报名活动: userId={}, activityId={}", userId, command.get("activityId"));
        return Result.success(activityAppService.registerActivity(userId, command));
    }

    // ==================== 13.4 取消报名 ====================

    @DeleteMapping("/register/{id}")
    @Operation(summary = "取消报名")
    public Result<Void> cancelRegistration(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("取消报名: userId={}, registrationId={}", userId, id);
        activityAppService.cancelRegistration(userId, id);
        return Result.success();
    }

    // ==================== 13.5 活动签到 ====================

    @PostMapping("/checkin")
    @Operation(summary = "活动签到")
    public Result<Map<String, Object>> checkinActivity(@RequestBody Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("活动签到: userId={}, activityId={}", userId, command.get("activityId"));
        return Result.success(activityAppService.checkinActivity(userId, command));
    }

    // ==================== 13.6 获取我的活动 ====================

    @GetMapping("/my")
    @Operation(summary = "获取我的活动")
    public Result<List<Map<String, Object>>> getMyActivities() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取我的活动: userId={}", userId);
        return Result.success(activityAppService.getMyActivities(userId));
    }
}
