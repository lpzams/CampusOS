package com.campus.application.card;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.domain.card.CampusCard;
import com.campus.domain.payment.PaymentOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CardAppService {
    private final CampusAppService records;
    public CardAppService(CampusAppService records) { this.records = records; }

    public Map<String, Object> info(Long userId) { return cardRow(userId); }
    public List<Map<String, Object>> transactions(Long userId) { return records.listExact("cardTransaction", "userId", userId); }

    @Transactional public Map<String, Object> reportLoss(Long userId) { return changeStatus(userId, true); }
    @Transactional public Map<String, Object> cancelLoss(Long userId) { return changeStatus(userId, false); }

    @Transactional
    public Map<String, Object> recharge(Long userId, Map<String, Object> command) {
        PaymentOrder.Method method;
        try { method = PaymentOrder.requireMethod(Values.required(command, "payMethod")); }
        catch (IllegalArgumentException e) { throw Values.invalid(e); }
        Map<String, Object> row = cardRow(userId);
        CampusCard card = toDomain(row);
        BigDecimal amount = Values.decimal(command.get("amount"));
        try { card.recharge(amount); } catch (RuntimeException e) { throw Values.invalid(e); }
        records.create("cardTransaction", Values.owned(userId,
                Map.of("type", "充值", "amount", amount, "payMethod", method.name(), "status", "成功")));
        return records.update("card", Values.longValue(row.get("id")), Map.of("balance", card.balance()));
    }

    private Map<String, Object> changeStatus(Long userId, boolean loss) {
        Map<String, Object> row = cardRow(userId);
        CampusCard card = toDomain(row);
        try { if (loss) card.reportLoss(); else card.cancelLoss(); }
        catch (RuntimeException e) { throw Values.invalid(e); }
        return records.update("card", Values.longValue(row.get("id")),
                Map.of("status", display(card.status())));
    }

    private Map<String, Object> cardRow(Long userId) {
        return records.listExact("card", "userId", userId).stream().findFirst()
                .orElseGet(() -> records.create("card", Map.of(
                        "userId", userId,
                        "cardId", String.format("C%08d", userId),
                        "realName", "校园用户",
                        "balance", BigDecimal.ZERO,
                        "status", "正常",
                        "expireTime", "2030-09-01")));
    }

    private CampusCard toDomain(Map<String, Object> row) {
        String value = String.valueOf(row.getOrDefault("status", "正常"));
        CampusCard.Status status = switch (value) { case "挂失" -> CampusCard.Status.LOST; case "冻结" -> CampusCard.Status.FROZEN; default -> CampusCard.Status.NORMAL; };
        return new CampusCard(Values.decimal(row.getOrDefault("balance", 0)), status);
    }

    private String display(CampusCard.Status status) {
        return switch (status) { case NORMAL -> "正常"; case LOST -> "挂失"; case FROZEN -> "冻结"; };
    }
}
