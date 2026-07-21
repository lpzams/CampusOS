package com.campus.application.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ElectricityRechargeDTO {
    private String orderId;
    private String dormitoryId;
    private BigDecimal amount;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private String payTime;
}
