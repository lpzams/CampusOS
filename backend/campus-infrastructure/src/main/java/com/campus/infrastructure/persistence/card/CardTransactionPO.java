package com.campus.infrastructure.persistence.card;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_card_transaction")
public class CardTransactionPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("card_id") private String cardId;
    @TableField("user_id") private Long userId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balance;
    private String merchant;
    private String description;
    @TableField("create_time") private LocalDateTime createTime;
}
