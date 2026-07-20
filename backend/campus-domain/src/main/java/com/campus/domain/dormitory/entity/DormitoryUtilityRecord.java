package com.campus.domain.dormitory.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DormitoryUtilityRecord {
    private Long id;
    private String dormitoryId;
    private String recordMonth;
    private BigDecimal electricityConsumption;
    private BigDecimal electricityCost;
    private BigDecimal waterConsumption;
    private BigDecimal waterCost;
    private LocalDateTime createTime;
}
