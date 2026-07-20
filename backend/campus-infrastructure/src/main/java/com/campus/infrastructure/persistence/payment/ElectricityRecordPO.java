package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_electricity_record")
public class ElectricityRecordPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id") private Long userId;
    @TableField("dormitory_id") private String dormitoryId;
    private BigDecimal amount;
    @TableField("old_balance") private BigDecimal oldBalance;
    @TableField("new_balance") private BigDecimal newBalance;
    @TableField("order_no") private String orderNo;
    @TableField("pay_method") private String payMethod;
    @TableField("pay_time") private LocalDateTime payTime;
    @TableField("create_time") private LocalDateTime createTime;
}
