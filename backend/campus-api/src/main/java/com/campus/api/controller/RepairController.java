package com.campus.api.controller;

import com.campus.application.repair.service.RepairAppService;
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
@RequestMapping("/api/repair")
@RequiredArgsConstructor
@Tag(name = "校园报修", description = "提交报修、报修列表、详情、评价相关接口")
public class RepairController {

    private final RepairAppService repairAppService;

    @PostMapping("/submit")
    @Operation(summary = "提交报修")
    public Result<Map<String, Object>> submitRepair(@RequestBody Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("提交报修: userId={}, title={}", userId, command.get("title"));
        return Result.success(repairAppService.submitRepair(userId, command));
    }

    @GetMapping("/list")
    @Operation(summary = "获取报修列表")
    public Result<List<Map<String, Object>>> getRepairList() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取报修列表: userId={}", userId);
        return Result.success(repairAppService.getRepairList(userId));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取报修详情")
    public Result<Map<String, Object>> getRepairDetail(@PathVariable Long id) {
        log.info("获取报修详情: id={}", id);
        return Result.success(repairAppService.getRepairDetail(id));
    }

    @PostMapping("/evaluate")
    @Operation(summary = "评价维修服务")
    public Result<Map<String, Object>> evaluateRepair(@RequestBody Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("评价维修: userId={}, repairId={}", userId, command.get("repairId"));
        return Result.success(repairAppService.evaluateRepair(userId, command));
    }
}
