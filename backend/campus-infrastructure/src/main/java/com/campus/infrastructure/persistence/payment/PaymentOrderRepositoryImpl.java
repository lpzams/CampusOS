package com.campus.infrastructure.persistence.payment;

import com.campus.domain.payment.entity.PaymentOrder;
import com.campus.domain.payment.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentOrderRepositoryImpl implements PaymentOrderRepository {
    private final PaymentOrderMapper mapper;
    private final PaymentConverter converter;

    @Override
    public List<PaymentOrder> findByUserId(Long userId, int offset, int size) {
        return converter.toPaymentOrderList(mapper.selectByUserId(userId, offset, size));
    }

    @Override
    public long countByUserId(Long userId) {
        return mapper.countByUserId(userId);
    }

    @Override
    public PaymentOrder findById(Long id) {
        return converter.toPaymentOrder(mapper.selectById(id));
    }

    @Override
    public void save(PaymentOrder order) {
        PaymentOrderPO po = converter.toPaymentOrderPO(order);
        mapper.insert(po);
        order.setId(po.getId());
    }

    @Override
    public void update(PaymentOrder order) {
        mapper.updateById(converter.toPaymentOrderPO(order));
    }
}
