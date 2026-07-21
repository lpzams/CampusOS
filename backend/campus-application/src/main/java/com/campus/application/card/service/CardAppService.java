package com.campus.application.card.service;

import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.card.entity.Card;
import com.campus.domain.card.entity.CardTransaction;
import com.campus.domain.card.repository.CardRepository;
import com.campus.domain.card.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardAppService {

    private final CardRepository cardRepository;
    private final CardTransactionRepository transactionRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 9.1 获取校园卡信息 ====================

    public Map<String, Object> getCardInfo(Long userId) {
        Card card = cardRepository.findByUserId(userId);
        if (card == null) {
            // 首次查询自动创建校园卡
            card = createDefaultCard(userId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cardId", card.getCardId());
        result.put("userId", card.getUserId());
        result.put("realName", card.getRealName());
        result.put("balance", card.getBalance());
        result.put("status", card.getStatusDesc());
        result.put("statusCode", card.getStatus());
        result.put("expireTime", card.getExpireTime() != null ? card.getExpireTime().format(DATE_FMT) : null);
        result.put("lastRechargeTime", card.getLastRechargeTime() != null ? card.getLastRechargeTime().format(FORMATTER) : null);
        result.put("lastConsumeTime", card.getLastConsumeTime() != null ? card.getLastConsumeTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 9.2 获取消费记录 ====================

    public PageResult<Map<String, Object>> getTransactions(Long userId, String startDate, String endDate, int page, int size) {
        Card card = cardRepository.findByUserId(userId);
        if (card == null) {
            return PageResult.empty();
        }

        int offset = (page - 1) * size;
        List<CardTransaction> transactions = transactionRepository.findByUserId(userId, startDate, endDate, offset, size);
        long total = transactionRepository.countByUserId(userId, startDate, endDate);

        List<Map<String, Object>> list = new ArrayList<>();
        for (CardTransaction t : transactions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("type", t.getTypeName());
            item.put("typeCode", t.getType());
            item.put("amount", t.getAmount());
            item.put("balance", t.getBalance());
            item.put("merchant", t.getMerchant());
            item.put("description", t.getDescription());
            item.put("createTime", t.getCreateTime() != null ? t.getCreateTime().format(FORMATTER) : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 9.3 校园卡挂失 ====================

    @Transactional
    public Map<String, Object> lossCard(Long userId) {
        Card card = getCard(userId);

        if (Card.STATUS_LOST.equals(card.getStatus())) {
            throw new BusinessException(ResultCode.CARD_ALREADY_LOST);
        }
        if (Card.STATUS_FROZEN.equals(card.getStatus())) {
            throw new BusinessException(ResultCode.CARD_FROZEN);
        }

        card.loss();
        cardRepository.update(card);

        log.info("校园卡挂失: cardId={}, userId={}", card.getCardId(), userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cardId", card.getCardId());
        result.put("status", "挂失");
        result.put("statusCode", "LOST");
        result.put("lossTime", LocalDateTime.now().format(FORMATTER));
        return result;
    }

    // ==================== 9.4 校园卡解挂 ====================

    @Transactional
    public Map<String, Object> unlossCard(Long userId) {
        Card card = getCard(userId);

        if (!Card.STATUS_LOST.equals(card.getStatus())) {
            throw new BusinessException(ResultCode.CARD_NOT_LOST);
        }

        card.unloss();
        cardRepository.update(card);

        log.info("校园卡解挂: cardId={}, userId={}", card.getCardId(), userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cardId", card.getCardId());
        result.put("status", "正常");
        result.put("statusCode", "NORMAL");
        result.put("unlossTime", LocalDateTime.now().format(FORMATTER));
        return result;
    }

    // ==================== 9.5 校园卡充值 ====================

    @Transactional
    public Map<String, Object> rechargeCard(Long userId, BigDecimal amount, String payMethod) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.CARD_BALANCE_INSUFFICIENT);
        }

        Card card = getCard(userId);

        if (Card.STATUS_LOST.equals(card.getStatus()) || Card.STATUS_FROZEN.equals(card.getStatus())) {
            throw new BusinessException("校园卡状态异常，无法充值");
        }

        BigDecimal oldBalance = card.getBalance();
        card.recharge(amount);
        cardRepository.update(card);

        // 生成交易记录
        String orderId = "RECHARGE" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        CardTransaction transaction = CardTransaction.create(
                card.getCardId(), userId, CardTransaction.TYPE_RECHARGE,
                amount, card.getBalance(), "线上充值", payMethod + "充值");
        transactionRepository.save(transaction);

        log.info("校园卡充值: cardId={}, amount={}, newBalance={}", card.getCardId(), amount, card.getBalance());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cardId", card.getCardId());
        result.put("amount", amount);
        result.put("oldBalance", oldBalance);
        result.put("newBalance", card.getBalance());
        result.put("orderId", orderId);
        result.put("payTime", LocalDateTime.now().format(FORMATTER));
        return result;
    }

    // ==================== 私有方法 ====================

    private Card getCard(Long userId) {
        Card card = cardRepository.findByUserId(userId);
        if (card == null) {
            card = createDefaultCard(userId);
        }
        return card;
    }

    private Card createDefaultCard(Long userId) {
        // 生成卡号
        String cardId = "C" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", userId % 10000);
        Card card = Card.create(cardId, userId, "");
        card.setExpireTime(java.time.LocalDate.now().plusYears(4));
        cardRepository.save(card);
        log.info("自动创建校园卡: cardId={}, userId={}", cardId, userId);
        return card;
    }
}
