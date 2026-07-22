package com.campus.domain.payment;

import java.math.BigDecimal;

public final class PaymentOrder {
    public enum Method { WECHAT, ALIPAY }

    private PaymentOrder() {}

    public static BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("金额必须大于 0");
        return amount;
    }

    public static Method requireMethod(String method) {
        try { return Method.valueOf(method); }
        catch (Exception e) { throw new IllegalArgumentException("支付方式必须为 WECHAT 或 ALIPAY"); }
    }
}
