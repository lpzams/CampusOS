package com.campus.infrastructure.persistence.card;

import com.campus.domain.card.entity.Card;
import com.campus.domain.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {
    private final CardMapper mapper;
    private final CardConverter converter;

    @Override
    public Card findByUserId(Long userId) {
        return converter.toCard(mapper.selectByUserId(userId));
    }

    @Override
    public Card findByCardId(String cardId) {
        return converter.toCard(mapper.selectByCardId(cardId));
    }

    @Override
    public void save(Card card) {
        CardPO po = converter.toCardPO(card);
        mapper.insert(po);
        card.setId(po.getId());
    }

    @Override
    public void update(Card card) {
        mapper.updateById(converter.toCardPO(card));
    }
}
