package com.campus.application.payment.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ElectricityRechargeCommand {
    @NotBlank private String dormitoryId;
    @NotNull private BigDecimal amount;
    @NotBlank private String payMethod;
}
