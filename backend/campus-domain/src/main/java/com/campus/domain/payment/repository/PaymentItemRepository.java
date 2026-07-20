package com.campus.domain.payment.repository;

import com.campus.domain.payment.entity.PaymentItem;
import java.util.List;

public interface PaymentItemRepository {
    List<PaymentItem> findAll(int offset, int size);
    long count();
    PaymentItem findById(Long id);
    void save(PaymentItem item);
    void update(PaymentItem item);
    void delete(Long id);
}
