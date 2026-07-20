package com.campus.application.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderResponseDTO {
    private String orderId;
    private BigDecimal amount;
    private String payMethod;
    private String status;
    private String payUrl;
    private String qrCode;
    private String expireTime;
}
