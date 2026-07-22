package com.campus.application.payment;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.domain.payment.PaymentOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PaymentAppService {
    private final CampusAppService records;
    public PaymentAppService(CampusAppService records) { this.records = records; }

    public List<Map<String, Object>> pending(Long userId) { return records.listExact("payment", "status", "待缴费").stream()
            .filter(row -> row.get("userId") == null || userId.toString().equals(String.valueOf(row.get("userId")))).toList(); }

    @Transactional
    public Map<String, Object> createOrder(Long userId, Map<String, Object> command) {
        long paymentId = Values.longValue(command.get("paymentId"));
        PaymentOrder.Method method = requireMethod(command.get("payMethod"));
        Map<String, Object> payment = records.get("payment", paymentId);
        Object owner = payment.get("userId");
        if (owner != null && !userId.toString().equals(String.valueOf(owner))) {
            throw Values.invalid(new IllegalArgumentException("无权操作该缴费项目"));
        }
        records.update("payment", paymentId, Map.of("status", "已缴费"));
        return records.create("paymentOrder", Values.owned(userId,
                Map.of("paymentId", paymentId,
                        "type", payment.getOrDefault("type", "校园缴费"),
                        "amount", payment.getOrDefault("amount", 0),
                        "payMethod", method.name(), "status", "支付成功")));
    }

    public List<Map<String, Object>> records(Long userId) { return records.listExact("paymentOrder", "userId", userId); }

    @Transactional
    public Map<String, Object> rechargeElectricity(Long userId, Map<String, Object> command) {
        String dormitoryId = Values.required(command, "dormitoryId");
        BigDecimal amount = requireAmount(command.get("amount"));
        PaymentOrder.Method method = requireMethod(command.get("payMethod"));
        Map<String, Object> dormitory = records.list("dormitory").stream()
                .filter(row -> userId.toString().equals(String.valueOf(row.get("userId"))))
                .findFirst().orElseThrow(() -> Values.invalid(new IllegalArgumentException("未找到当前用户的宿舍信息")));
        BigDecimal balance = Values.decimal(dormitory.getOrDefault("electricityBalance", 0)).add(amount);
        records.update("dormitory", Values.longValue(dormitory.get("id")), Map.of("electricityBalance", balance));
        return records.create("paymentOrder", Values.owned(userId, Map.of("type", "电费充值",
                "dormitoryId", dormitoryId, "amount", amount, "payMethod", method.name(), "status", "支付成功")));
    }

    private BigDecimal requireAmount(Object value) {
        try { return PaymentOrder.requireAmount(Values.decimal(value)); }
        catch (IllegalArgumentException e) { throw Values.invalid(e); }
    }

    private PaymentOrder.Method requireMethod(Object value) {
        try { return PaymentOrder.requireMethod(value == null ? null : value.toString()); }
        catch (IllegalArgumentException e) { throw Values.invalid(e); }
    }
}
