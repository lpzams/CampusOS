package com.campus.domain.payment.repository;

import com.campus.domain.payment.entity.Payment;

import java.util.List;

public interface PaymentRepository {
    List<Payment> findByUserId(Long userId);
    List<Payment> findPendingByUserId(Long userId);
    Payment findById(Long id);
    void save(Payment payment);
    void update(Payment payment);
    void saveBatch(List<Payment> payments);
}
