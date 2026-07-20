package com.campus.domain.product.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class ProductOrder {
    private Long id;
    private String orderId;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal price;
    private String message;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 状态常量 =====
    public static final String STATUS_PENDING = "PENDING";

    // ===== 状态描述 =====
    public String getStatusDesc() {
        if (STATUS_PENDING.equals(status)) return "待确认";
        return status;
    }

    // ===== 工厂方法 =====

    public static ProductOrder create(Long productId, Long buyerId, Long sellerId,
                                      BigDecimal price, String message) {
        ProductOrder o = new ProductOrder();
        o.setOrderId(generateOrderId());
        o.setProductId(productId);
        o.setBuyerId(buyerId);
        o.setSellerId(sellerId);
        o.setPrice(price);
        o.setMessage(message);
        o.setStatus(STATUS_PENDING);
        o.setCreateTime(LocalDateTime.now());
        o.setUpdateTime(LocalDateTime.now());
        return o;
    }

    /** 生成订单编号 ORDER + yyyyMMdd + 6位随机数 */
    private static String generateOrderId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "ORDER" + date + random;
    }
}
