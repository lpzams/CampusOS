package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_electricity_balance")
public class ElectricityBalancePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("dormitory_id") private String dormitoryId;
    private BigDecimal balance;
    @TableField("update_time") private LocalDateTime updateTime;
}
