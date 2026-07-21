package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_payment_item")
public class PaymentItemPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    @TableField("type_name") private String typeName;
    private BigDecimal amount;
    private LocalDate deadline;
    private String status;
    private String description;
    @TableField("target_users") private String targetUsers;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}
