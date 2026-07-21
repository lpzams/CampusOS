package com.campus.application.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentRecordDTO {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String status;
    private String payMethod;
    private String payTime;
    private String orderId;
}
