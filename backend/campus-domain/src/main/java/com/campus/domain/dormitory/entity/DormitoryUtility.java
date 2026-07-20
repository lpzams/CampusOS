package com.campus.domain.dormitory.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DormitoryUtility {
    private Long id;
    private String dormitoryId;
    private BigDecimal electricityBalance;
    private BigDecimal waterBalance;
    private LocalDateTime updateTime;
}
