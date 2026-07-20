package com.campus.domain.payment.repository;

import com.campus.domain.payment.entity.PaymentOrder;

import java.util.List;

public interface PaymentOrderRepository {
    List<PaymentOrder> findByUserId(Long userId, int offset, int size);
    long countByUserId(Long userId);
    PaymentOrder findById(Long id);
    void save(PaymentOrder order);
    void update(PaymentOrder order);
}
