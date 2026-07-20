package com.campus.domain.card.entity;

import com.campus.common.exception.BusinessException;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Card {

    private Long id;
    private String cardId;
    private Long userId;
    private String realName;
    private BigDecimal balance;
    private String status;
    private LocalDate expireTime;
    private LocalDateTime lastRechargeTime;
    private LocalDateTime lastConsumeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========
    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_LOST = "LOST";
    public static final String STATUS_FROZEN = "FROZEN";

    public String getStatusDesc() {
        if (STATUS_NORMAL.equals(status)) return "正常";
        if (STATUS_LOST.equals(status)) return "挂失";
        if (STATUS_FROZEN.equals(status)) return "冻结";
        return status;
    }

    // ========== 业务方法 ==========

    /** 挂失 */
    public void loss() {
        if (STATUS_LOST.equals(this.status)) {
            throw new BusinessException("校园卡已挂失");
        }
        this.status = STATUS_LOST;
        this.updateTime = LocalDateTime.now();
    }

    /** 解挂 */
    public void unloss() {
        if (!STATUS_LOST.equals(this.status)) {
            throw new BusinessException("校园卡未挂失");
        }
        this.status = STATUS_NORMAL;
        this.updateTime = LocalDateTime.now();
    }

    /** 充值 */
    public void recharge(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        this.balance = this.balance.add(amount);
        this.lastRechargeTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /** 消费 */
    public void consume(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }
        this.balance = this.balance.subtract(amount);
        this.lastConsumeTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // ========== 工厂方法 ==========

    public static Card create(String cardId, Long userId, String realName) {
        Card card = new Card();
        card.setCardId(cardId);
        card.setUserId(userId);
        card.setRealName(realName);
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(STATUS_NORMAL);
        card.setCreateTime(LocalDateTime.now());
        card.setUpdateTime(LocalDateTime.now());
        return card;
    }
}
