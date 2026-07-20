package com.campus.domain.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class PaymentOrder {

    private Long id;
    private Long paymentId;
    private Long userId;
    private String orderNo;
    private BigDecimal amount;
    private String payMethod;
    private String status;
    private String payUrl;
    private String qrCode;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";

    // ========== 工厂方法 ==========

    public static PaymentOrder create(Long paymentId, Long userId, BigDecimal amount, String payMethod) {
        String orderNo = "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        PaymentOrder order = new PaymentOrder();
        order.setPaymentId(paymentId);
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(amount);
        order.setPayMethod(payMethod);
        order.setStatus(STATUS_PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return order;
    }
}
