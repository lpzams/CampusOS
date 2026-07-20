package com.campus.infrastructure.persistence.card;

import com.campus.domain.card.entity.Card;
import com.campus.domain.card.entity.CardTransaction;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardConverter {
    public Card toCard(CardPO po) { if (po == null) return null; Card c = new Card(); BeanUtils.copyProperties(po, c); return c; }
    public CardPO toCardPO(Card c) { if (c == null) return null; CardPO po = new CardPO(); BeanUtils.copyProperties(c, po); return po; }
    public CardTransaction toTransaction(CardTransactionPO po) { if (po == null) return null; CardTransaction t = new CardTransaction(); BeanUtils.copyProperties(po, t); return t; }
    public CardTransactionPO toTransactionPO(CardTransaction t) { if (t == null) return null; CardTransactionPO po = new CardTransactionPO(); BeanUtils.copyProperties(t, po); return po; }
    public List<CardTransaction> toTransactionList(List<CardTransactionPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toTransaction).collect(Collectors.toList());
    }
}
