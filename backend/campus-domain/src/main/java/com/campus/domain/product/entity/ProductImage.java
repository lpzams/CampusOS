package com.campus.domain.product.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductImage {
    private Long id;
    private Long productId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createTime;

    public static ProductImage create(Long productId, String imageUrl, Integer sortOrder) {
        ProductImage pi = new ProductImage();
        pi.setProductId(productId);
        pi.setImageUrl(imageUrl);
        pi.setSortOrder(sortOrder != null ? sortOrder : 0);
        pi.setCreateTime(LocalDateTime.now());
        return pi;
    }
}
