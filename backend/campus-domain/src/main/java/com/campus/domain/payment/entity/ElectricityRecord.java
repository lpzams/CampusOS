package com.campus.domain.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ElectricityRecord {

    private Long id;
    private Long userId;
    private String dormitoryId;
    private BigDecimal amount;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private String orderNo;
    private String payMethod;
    private LocalDateTime payTime;
    private LocalDateTime createTime;

    // ========== 工厂方法 ==========

    public static ElectricityRecord create(Long userId, String dormitoryId, BigDecimal amount,
                                            BigDecimal oldBalance, BigDecimal newBalance,
                                            String payMethod) {
        String orderNo = "ELEC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        ElectricityRecord record = new ElectricityRecord();
        record.setUserId(userId);
        record.setDormitoryId(dormitoryId);
        record.setAmount(amount);
        record.setOldBalance(oldBalance);
        record.setNewBalance(newBalance);
        record.setOrderNo(orderNo);
        record.setPayMethod(payMethod);
        record.setPayTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        return record;
    }
}
