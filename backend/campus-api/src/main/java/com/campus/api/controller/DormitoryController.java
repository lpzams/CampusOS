package com.campus.api.controller;

import com.campus.application.dormitory.service.DormitoryAppService;
import com.campus.application.user.service.UserAppService;
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
@RequestMapping("/api/dormitory")
@RequiredArgsConstructor
@Tag(name = "宿舍管理", description = "宿舍信息、宿舍公告、水电余额相关接口")
public class DormitoryController {

    private final DormitoryAppService dormitoryAppService;

    // ==================== 10.1 获取宿舍信息 ====================

    @GetMapping("/info")
    @Operation(summary = "获取宿舍信息")
    public Result<Map<String, Object>> getDormitoryInfo() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取宿舍信息: userId={}", userId);
        return Result.success(dormitoryAppService.getDormitoryInfo(userId));
    }

    // ==================== 10.2 获取宿舍公告 ====================

    @GetMapping("/notice")
    @Operation(summary = "获取宿舍公告")
    public Result<List<Map<String, Object>>> getDormitoryNotices() {
        log.info("获取宿舍公告");
        return Result.success(dormitoryAppService.getDormitoryNotices());
    }

    // ==================== 10.3 查询水电余额 ====================

    @GetMapping("/utility")
    @Operation(summary = "查询水电余额")
    public Result<Map<String, Object>> getDormitoryUtility() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("查询水电余额: userId={}", userId);
        return Result.success(dormitoryAppService.getDormitoryUtility(userId));
    }
}
