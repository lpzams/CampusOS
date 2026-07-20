package com.campus.api.controller;

import com.campus.application.product.service.ProductAppService;
import com.campus.application.user.service.UserAppService;
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
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "二手交易", description = "商品列表、详情、发布、收藏、订单、上下架相关接口")
public class ProductController {

    private final ProductAppService productAppService;

    // ==================== 12.1 获取商品列表 ====================

    @GetMapping("/list")
    @Operation(summary = "获取商品列表")
    public Result<PageResult<Map<String, Object>>> getProductList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取商品列表: categoryId={}, keyword={}, minPrice={}, maxPrice={}, status={}, page={}",
                categoryId, keyword, minPrice, maxPrice, status, page);
        return Result.success(productAppService.getProductList(categoryId, keyword, minPrice, maxPrice, status, page, size));
    }

    // ==================== 12.2 获取商品详情 ====================

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Long currentUserId = null;
        try {
            currentUserId = UserAppService.getCurrentUserId();
        } catch (Exception ignored) {
            // 未登录用户也可以浏览详情
        }
        log.info("获取商品详情: id={}, userId={}", id, currentUserId);
        return Result.success(productAppService.getProductDetail(id, currentUserId));
    }

    // ==================== 12.3 发布商品 ====================

    @PostMapping
    @Operation(summary = "发布商品")
    public Result<Map<String, Object>> publishProduct(@RequestBody Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("发布商品: userId={}, title={}", userId, command.get("title"));
        return Result.success(productAppService.publishProduct(userId, command));
    }

    // ==================== 12.4 收藏商品 ====================

    @PostMapping("/favorite/{id}")
    @Operation(summary = "收藏商品")
    public Result<Map<String, Object>> favoriteProduct(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("收藏商品: userId={}, productId={}", userId, id);
        return Result.success(productAppService.favoriteProduct(userId, id));
    }

    // ==================== 12.5 创建订单 ====================

    @PostMapping("/order")
    @Operation(summary = "创建订单")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("创建订单: buyerId={}, productId={}", userId, command.get("productId"));
        return Result.success(productAppService.createOrder(userId, command));
    }

    // ==================== 12.6 下架商品 ====================

    @PutMapping("/{id}/off-shelf")
    @Operation(summary = "下架商品")
    public Result<Map<String, Object>> offShelfProduct(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("下架商品: userId={}, productId={}", userId, id);
        return Result.success(productAppService.offShelfProduct(userId, id));
    }

    // ==================== 12.7 重新上架商品 ====================

    @PutMapping("/{id}/on-shelf")
    @Operation(summary = "重新上架商品")
    public Result<Map<String, Object>> onShelfProduct(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("上架商品: userId={}, productId={}", userId, id);
        return Result.success(productAppService.onShelfProduct(userId, id));
    }

    // ==================== 12.8 标记已售 ====================

    @PutMapping("/{id}/sold")
    @Operation(summary = "标记已售")
    public Result<Map<String, Object>> markSoldProduct(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("标记已售: userId={}, productId={}", userId, id);
        return Result.success(productAppService.markSoldProduct(userId, id));
    }

    // ==================== 12.10 我的商品列表 ====================

    @GetMapping("/my")
    @Operation(summary = "我的商品列表")
    public Result<PageResult<Map<String, Object>>> getMyProducts(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("我的商品列表: userId={}, status={}, page={}", userId, status, page);
        return Result.success(productAppService.getMyProducts(userId, status, page, size));
    }
}
