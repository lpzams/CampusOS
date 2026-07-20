package com.campus.application.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentDTO {
    private Long id;
    private String type;
    private String typeCode;
    private BigDecimal amount;
    private String deadline;
    private String status;
    private String statusCode;
    private String description;
    private BigDecimal lateFee;
}
