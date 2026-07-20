package com.campus.domain.payment.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentItem {

    private Long id;
    private String type;
    private String typeName;
    private BigDecimal amount;
    private LocalDate deadline;
    private String status;
    private String description;
    private String targetUsers;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CLOSED = "CLOSED";

    public String getStatusDesc() {
        return STATUS_ACTIVE.equals(status) ? "进行中" : "已关闭";
    }

    // ========== 工厂方法 ==========

    public static PaymentItem create(String type, String typeName, BigDecimal amount,
                                      LocalDate deadline, String description, String targetUsers) {
        PaymentItem item = new PaymentItem();
        item.setType(type);
        item.setTypeName(typeName);
        item.setAmount(amount);
        item.setDeadline(deadline);
        item.setStatus(STATUS_ACTIVE);
        item.setDescription(description);
        item.setTargetUsers(targetUsers);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }

    // ========== 业务方法 ==========

    public void updateFields(BigDecimal amount, LocalDate deadline, String description) {
        if (amount != null) this.amount = amount;
        if (deadline != null) this.deadline = deadline;
        if (description != null) this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    public void close() {
        this.status = STATUS_CLOSED;
        this.updateTime = LocalDateTime.now();
    }
}
