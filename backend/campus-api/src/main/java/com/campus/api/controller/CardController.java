package com.campus.api.controller;

import com.campus.application.card.service.CardAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
@Tag(name = "校园卡服务", description = "校园卡信息、消费记录、挂失/解挂、充值相关接口")
public class CardController {

    private final CardAppService cardAppService;

    // ==================== 9.1 获取校园卡信息 ====================

    @GetMapping("/info")
    @Operation(summary = "获取校园卡信息")
    public Result<Map<String, Object>> getCardInfo() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取校园卡信息: userId={}", userId);
        return Result.success(cardAppService.getCardInfo(userId));
    }

    // ==================== 9.2 获取消费记录 ====================

    @GetMapping("/transactions")
    @Operation(summary = "获取消费记录")
    public Result<PageResult<Map<String, Object>>> getTransactions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取消费记录: userId={}, page={}", userId, page);
        return Result.success(cardAppService.getTransactions(userId, startDate, endDate, page, size));
    }

    // ==================== 9.3 校园卡挂失 ====================

    @PostMapping("/loss")
    @Operation(summary = "校园卡挂失")
    public Result<Map<String, Object>> lossCard() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("校园卡挂失: userId={}", userId);
        return Result.success(cardAppService.lossCard(userId));
    }

    // ==================== 9.4 校园卡解挂 ====================

    @PostMapping("/unloss")
    @Operation(summary = "校园卡解挂")
    public Result<Map<String, Object>> unlossCard() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("校园卡解挂: userId={}", userId);
        return Result.success(cardAppService.unlossCard(userId));
    }

    // ==================== 9.5 校园卡充值 ====================

    @PostMapping("/recharge")
    @Operation(summary = "校园卡充值")
    public Result<Map<String, Object>> rechargeCard(@RequestBody Map<String, Object> body) {
        Long userId = UserAppService.getCurrentUserId();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String payMethod = (String) body.get("payMethod");
        log.info("校园卡充值: userId={}, amount={}, payMethod={}", userId, amount, payMethod);
        return Result.success(cardAppService.rechargeCard(userId, amount, payMethod));
    }
}
