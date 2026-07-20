package com.campus.infrastructure.persistence.payment;

import com.campus.domain.payment.entity.PaymentItem;
import com.campus.domain.payment.repository.PaymentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentItemRepositoryImpl implements PaymentItemRepository {
    private final PaymentItemMapper mapper;
    private final PaymentConverter converter;

    @Override
    public List<PaymentItem> findAll(int offset, int size) {
        return converter.toPaymentItemList(mapper.selectPage(offset, size));
    }

    @Override
    public long count() {
        return mapper.count();
    }

    @Override
    public PaymentItem findById(Long id) {
        return converter.toPaymentItem(mapper.selectById(id));
    }

    @Override
    public void save(PaymentItem item) {
        PaymentItemPO po = converter.toPaymentItemPO(item);
        mapper.insert(po);
        item.setId(po.getId());
    }

    @Override
    public void update(PaymentItem item) {
        mapper.updateById(converter.toPaymentItemPO(item));
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    public int countPaid(Long itemId) {
        return mapper.countPaidByItemId(itemId);
    }

    public int countTotal(Long itemId) {
        return mapper.countTotalByItemId(itemId);
    }
}
