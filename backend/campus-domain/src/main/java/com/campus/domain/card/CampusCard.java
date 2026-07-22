package com.campus.domain.card;

import java.math.BigDecimal;

public class CampusCard {
    public enum Status { NORMAL, LOST, FROZEN }

    private BigDecimal balance;
    private Status status;

    public CampusCard(BigDecimal balance, Status status) {
        this.balance = balance == null ? BigDecimal.ZERO : balance;
        this.status = status == null ? Status.NORMAL : status;
    }

    public void reportLoss() {
        if (status == Status.FROZEN) throw new IllegalStateException("冻结卡不能挂失");
        status = Status.LOST;
    }

    public void cancelLoss() {
        if (status != Status.LOST) throw new IllegalStateException("只有挂失状态可以解挂");
        status = Status.NORMAL;
    }

    public void recharge(BigDecimal amount) {
        if (status != Status.NORMAL) throw new IllegalStateException("校园卡状态异常，不能充值");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("充值金额必须大于 0");
        balance = balance.add(amount);
    }

    public BigDecimal balance() { return balance; }
    public Status status() { return status; }
}
