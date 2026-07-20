package com.campus.infrastructure.persistence.payment;

import com.campus.domain.payment.entity.Payment;
import com.campus.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentMapper paymentMapper;
    private final PaymentConverter converter;

    @Override
    public List<Payment> findByUserId(Long userId) {
        return converter.toPaymentList(paymentMapper.selectByUserId(userId));
    }

    @Override
    public List<Payment> findPendingByUserId(Long userId) {
        return converter.toPaymentList(paymentMapper.selectPendingByUserId(userId));
    }

    @Override
    public Payment findById(Long id) {
        return converter.toPayment(paymentMapper.selectById(id));
    }

    @Override
    public void save(Payment payment) {
        PaymentPO po = converter.toPaymentPO(payment);
        paymentMapper.insert(po);
        payment.setId(po.getId());
    }

    @Override
    public void update(Payment payment) {
        paymentMapper.updateById(converter.toPaymentPO(payment));
    }

    @Override
    public void saveBatch(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) return;
        for (Payment p : payments) {
            save(p);
        }
    }
}
