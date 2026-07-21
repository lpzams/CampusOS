package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_payment")
public class PaymentPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("item_id") private Long itemId;
    @TableField("user_id") private Long userId;
    private String type;
    @TableField("type_code") private String typeCode;
    private BigDecimal amount;
    private LocalDate deadline;
    private String status;
    private String description;
    @TableField("late_fee") private BigDecimal lateFee;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}
