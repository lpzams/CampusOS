package com.campus.application.payment.service;

import com.campus.application.payment.command.*;
import com.campus.application.payment.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.payment.entity.*;
import com.campus.domain.payment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAppService {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository orderRepository;
    private final ElectricityRepository electricityRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 8.1 获取待缴费列表 ====================

    public List<PaymentDTO> getPendingPayments(Long userId) {
        List<Payment> payments = paymentRepository.findPendingByUserId(userId);
        return payments.stream().map(p -> PaymentDTO.builder()
                .id(p.getId())
                .type(p.getType())
                .typeCode(p.getTypeCode())
                .amount(p.getAmount())
                .deadline(p.getDeadline() != null ? p.getDeadline().format(DATE_FMT) : null)
                .status(p.getStatusDesc())
                .statusCode(p.getStatus())
                .description(p.getDescription())
                .lateFee(p.getLateFee())
                .build()).collect(Collectors.toList());
    }

    // ==================== 8.2 创建缴费订单 ====================

    @Transactional
    public OrderResponseDTO createOrder(Long userId, CreateOrderCommand command) {
        Payment payment = paymentRepository.findById(command.getPaymentId());
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        if (!payment.isPending()) {
            throw new BusinessException(ResultCode.PAYMENT_ALREADY_PAID);
        }

        PaymentOrder order = PaymentOrder.create(payment.getId(), userId, payment.getAmount(), command.getPayMethod());
        // Mock 支付URL
        order.setPayUrl("https://pay.example.com/order/" + order.getOrderNo());
        order.setQrCode("https://cdn.example.com/qrcode/" + order.getOrderNo() + ".png");
        orderRepository.save(order);

        log.info("缴费订单创建: orderNo={}, amount={}", order.getOrderNo(), order.getAmount());

        return OrderResponseDTO.builder()
                .orderId(order.getOrderNo())
                .amount(order.getAmount())
                .payMethod(order.getPayMethod())
                .status("待支付")
                .payUrl(order.getPayUrl())
                .qrCode(order.getQrCode())
                .expireTime(order.getExpireTime() != null ? order.getExpireTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 8.3 获取缴费记录 ====================

    public PageResult<PaymentRecordDTO> getPaymentRecords(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<PaymentOrder> orders = orderRepository.findByUserId(userId, offset, size);
        long total = orderRepository.countByUserId(userId);

        List<PaymentRecordDTO> list = new ArrayList<>();
        for (PaymentOrder order : orders) {
            Payment payment = paymentRepository.findById(order.getPaymentId());
            list.add(PaymentRecordDTO.builder()
                    .id(order.getId())
                    .type(payment != null ? payment.getType() : "未知")
                    .amount(order.getAmount())
                    .status(order.getStatus())
                    .payMethod(order.getPayMethod())
                    .payTime(order.getPayTime() != null ? order.getPayTime().format(FORMATTER) : null)
                    .orderId(order.getOrderNo())
                    .build());
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 8.4 电费充值 ====================

    @Transactional
    public ElectricityRechargeDTO rechargeElectricity(Long userId, ElectricityRechargeCommand command) {
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PAYMENT_AMOUNT_ERROR);
        }

        BigDecimal oldBalance = electricityRepository.getBalance(command.getDormitoryId());
        BigDecimal newBalance = oldBalance.add(command.getAmount());
        electricityRepository.updateBalance(command.getDormitoryId(), newBalance);

        ElectricityRecord record = ElectricityRecord.create(
                userId, command.getDormitoryId(), command.getAmount(),
                oldBalance, newBalance, command.getPayMethod());
        electricityRepository.saveRecord(record);

        log.info("电费充值成功: dormitoryId={}, amount={}, newBalance={}",
                command.getDormitoryId(), command.getAmount(), newBalance);

        return ElectricityRechargeDTO.builder()
                .orderId(record.getOrderNo())
                .dormitoryId(command.getDormitoryId())
                .amount(command.getAmount())
                .oldBalance(oldBalance)
                .newBalance(newBalance)
                .payTime(record.getPayTime() != null ? record.getPayTime().format(FORMATTER) : null)
                .build();
    }
}
