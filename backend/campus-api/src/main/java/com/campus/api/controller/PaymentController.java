package com.campus.api.controller;

import com.campus.application.payment.command.*;
import com.campus.application.payment.dto.*;
import com.campus.application.payment.service.PaymentAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "校园缴费", description = "待缴费、创建订单、缴费记录、电费充值相关接口")
public class PaymentController {

    private final PaymentAppService paymentAppService;

    // ==================== 8.1 获取待缴费列表 ====================

    @GetMapping("/pending")
    @Operation(summary = "获取待缴费列表")
    public Result<List<PaymentDTO>> getPendingPayments() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取待缴费列表: userId={}", userId);
        return Result.success(paymentAppService.getPendingPayments(userId));
    }

    // ==================== 8.2 创建缴费订单 ====================

    @PostMapping("/order")
    @Operation(summary = "创建缴费订单")
    public Result<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("创建缴费订单: userId={}, paymentId={}, payMethod={}", userId, command.getPaymentId(), command.getPayMethod());
        return Result.success(paymentAppService.createOrder(userId, command));
    }

    // ==================== 8.3 获取缴费记录 ====================

    @GetMapping("/records")
    @Operation(summary = "获取缴费记录")
    public Result<PageResult<PaymentRecordDTO>> getPaymentRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取缴费记录: userId={}, page={}, size={}", userId, page, size);
        return Result.success(paymentAppService.getPaymentRecords(userId, page, size));
    }

    // ==================== 8.4 电费充值 ====================

    @PostMapping("/electricity")
    @Operation(summary = "电费充值")
    public Result<ElectricityRechargeDTO> rechargeElectricity(@Valid @RequestBody ElectricityRechargeCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("电费充值: userId={}, dormitoryId={}, amount={}", userId, command.getDormitoryId(), command.getAmount());
        return Result.success(paymentAppService.rechargeElectricity(userId, command));
    }
}
