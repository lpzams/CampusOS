package com.campus.domain.card.repository;

import com.campus.domain.card.entity.CardTransaction;
import java.util.List;

public interface CardTransactionRepository {
    List<CardTransaction> findByUserId(Long userId, String startDate, String endDate, int offset, int size);
    long countByUserId(Long userId, String startDate, String endDate);
    void save(CardTransaction transaction);
}
