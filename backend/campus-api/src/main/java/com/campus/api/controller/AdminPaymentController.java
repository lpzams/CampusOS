package com.campus.api.controller;

import com.campus.application.payment.service.AdminPaymentService;
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
@RequestMapping("/api/admin/payment")
@RequiredArgsConstructor
@Tag(name = "缴费项目管理", description = "管理员缴费项目创建、更新、删除、列表相关接口")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    // ==================== 8.5 获取缴费项目列表 ====================

    @GetMapping("/items")
    @Operation(summary = "获取缴费项目列表（管理员）")
    public Result<PageResult<Map<String, Object>>> getPaymentItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取缴费项目列表: page={}, size={}", page, size);
        return Result.success(adminPaymentService.getPaymentItems(page, size));
    }

    // ==================== 8.6 创建缴费项目 ====================

    @PostMapping("/item")
    @Operation(summary = "创建缴费项目（管理员）")
    public Result<Map<String, Object>> createPaymentItem(@RequestBody Map<String, Object> command) {
        log.info("创建缴费项目: type={}, typeName={}", command.get("type"), command.get("typeName"));
        return Result.success(adminPaymentService.createPaymentItem(command));
    }

    // ==================== 8.7 更新缴费项目 ====================

    @PutMapping("/item/{id}")
    @Operation(summary = "更新缴费项目（管理员）")
    public Result<Map<String, Object>> updatePaymentItem(@PathVariable Long id,
                                                          @RequestBody Map<String, Object> command) {
        log.info("更新缴费项目: id={}", id);
        return Result.success(adminPaymentService.updatePaymentItem(id, command));
    }

    // ==================== 8.8 删除缴费项目 ====================

    @DeleteMapping("/item/{id}")
    @Operation(summary = "删除缴费项目（管理员）")
    public Result<Void> deletePaymentItem(@PathVariable Long id) {
        log.info("删除缴费项目: id={}", id);
        adminPaymentService.deletePaymentItem(id);
        return Result.success();
    }
}
