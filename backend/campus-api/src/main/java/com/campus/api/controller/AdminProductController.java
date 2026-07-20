package com.campus.api.controller;

import com.campus.application.product.service.ProductAppService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
@Tag(name = "二手交易管理", description = "管理员删除商品相关接口")
public class AdminProductController {

    private final ProductAppService productAppService;

    // ==================== 12.9 删除商品（管理员） ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品（管理员）")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        log.info("管理员删除商品: productId={}", id);
        productAppService.deleteProduct(id);
        return Result.success();
    }
}
