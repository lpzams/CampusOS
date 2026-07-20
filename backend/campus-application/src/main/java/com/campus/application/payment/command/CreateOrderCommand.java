package com.campus.application.payment.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderCommand {
    @NotNull private Long paymentId;
    @NotBlank private String payMethod;
}
