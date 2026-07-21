package com.campus.infrastructure.persistence.card;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_card")
public class CardPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("card_id") private String cardId;
    @TableField("user_id") private Long userId;
    @TableField("real_name") private String realName;
    private BigDecimal balance;
    private String status;
    @TableField("expire_time") private LocalDate expireTime;
    @TableField("last_recharge_time") private LocalDateTime lastRechargeTime;
    @TableField("last_consume_time") private LocalDateTime lastConsumeTime;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
}
