package com.campus.domain.product.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductCategory {
    private Integer id;
    private String name;
    private LocalDateTime createTime;
}
