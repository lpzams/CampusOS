package com.campus.domain.card.repository;

import com.campus.domain.card.entity.Card;

public interface CardRepository {
    Card findByUserId(Long userId);
    Card findByCardId(String cardId);
    void save(Card card);
    void update(Card card);
}
