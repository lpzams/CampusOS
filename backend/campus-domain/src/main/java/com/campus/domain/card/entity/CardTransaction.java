package com.campus.domain.card.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CardTransaction {

    private Long id;
    private String cardId;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balance;
    private String merchant;
    private String description;
    private LocalDateTime createTime;

    // ========== 常量 ==========
    public static final String TYPE_CONSUME = "CONSUME";
    public static final String TYPE_RECHARGE = "RECHARGE";

    public String getTypeName() {
        return TYPE_CONSUME.equals(type) ? "消费" : "充值";
    }

    // ========== 工厂方法 ==========

    public static CardTransaction create(String cardId, Long userId, String type,
                                          BigDecimal amount, BigDecimal balance,
                                          String merchant, String description) {
        CardTransaction t = new CardTransaction();
        t.setCardId(cardId);
        t.setUserId(userId);
        t.setType(type);
        t.setAmount(amount);
        t.setBalance(balance);
        t.setMerchant(merchant);
        t.setDescription(description);
        t.setCreateTime(LocalDateTime.now());
        return t;
    }
}
