package com.campus.domain.product.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductFavorite {
    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime createTime;

    public static ProductFavorite create(Long userId, Long productId) {
        ProductFavorite pf = new ProductFavorite();
        pf.setUserId(userId);
        pf.setProductId(productId);
        pf.setCreateTime(LocalDateTime.now());
        return pf;
    }
}
