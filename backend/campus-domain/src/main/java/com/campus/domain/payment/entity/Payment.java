package com.campus.domain.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Payment {

    private Long id;
    private Long itemId;
    private Long userId;
    private String type;
    private String typeCode;
    private BigDecimal amount;
    private LocalDate deadline;
    private String status;
    private String description;
    private BigDecimal lateFee;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_OVERDUE = "OVERDUE";

    public static final String TYPE_TUITION = "TUITION";
    public static final String TYPE_DORMITORY = "DORMITORY";
    public static final String TYPE_BOOK = "BOOK";

    // ========== 业务方法 ==========

    public String getStatusDesc() {
        if (STATUS_PENDING.equals(status)) return "待缴费";
        if (STATUS_PAID.equals(status)) return "已缴费";
        if (STATUS_OVERDUE.equals(status)) return "已逾期";
        return status;
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /** 标记已缴费 */
    public void markPaid() {
        this.status = STATUS_PAID;
        this.updateTime = LocalDateTime.now();
    }

    // ========== 工厂方法 ==========

    public static Payment create(Long userId, String type, String typeCode, BigDecimal amount,
                                  LocalDate deadline, String description) {
        Payment p = new Payment();
        p.setUserId(userId);
        p.setType(type);
        p.setTypeCode(typeCode);
        p.setAmount(amount);
        p.setDeadline(deadline);
        p.setStatus(STATUS_PENDING);
        p.setDescription(description);
        p.setLateFee(BigDecimal.ZERO);
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        return p;
    }
}
