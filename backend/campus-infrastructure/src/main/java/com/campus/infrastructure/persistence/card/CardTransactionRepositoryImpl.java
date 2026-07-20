package com.campus.infrastructure.persistence.card;

import com.campus.domain.card.entity.CardTransaction;
import com.campus.domain.card.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CardTransactionRepositoryImpl implements CardTransactionRepository {
    private final CardTransactionMapper mapper;
    private final CardConverter converter;

    @Override
    public List<CardTransaction> findByUserId(Long userId, String startDate, String endDate, int offset, int size) {
        return converter.toTransactionList(mapper.selectByUserId(userId, startDate, endDate, offset, size));
    }

    @Override
    public long countByUserId(Long userId, String startDate, String endDate) {
        return mapper.countByUserId(userId, startDate, endDate);
    }

    @Override
    public void save(CardTransaction transaction) {
        mapper.insert(converter.toTransactionPO(transaction));
    }
}
