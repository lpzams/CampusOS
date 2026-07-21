package com.campus.infrastructure.persistence.payment;

import com.campus.domain.payment.entity.ElectricityRecord;
import com.campus.domain.payment.entity.Payment;
import com.campus.domain.payment.entity.PaymentItem;
import com.campus.domain.payment.entity.PaymentOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentConverter {

    public Payment toPayment(PaymentPO po) {
        if (po == null) return null;
        Payment p = new Payment();
        BeanUtils.copyProperties(po, p);
        return p;
    }
    public PaymentPO toPaymentPO(Payment p) {
        if (p == null) return null;
        PaymentPO po = new PaymentPO();
        BeanUtils.copyProperties(p, po);
        return po;
    }
    public List<Payment> toPaymentList(List<PaymentPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toPayment).collect(Collectors.toList());
    }

    public PaymentOrder toPaymentOrder(PaymentOrderPO po) {
        if (po == null) return null;
        PaymentOrder o = new PaymentOrder();
        BeanUtils.copyProperties(po, o);
        return o;
    }
    public PaymentOrderPO toPaymentOrderPO(PaymentOrder order) {
        if (order == null) return null;
        PaymentOrderPO po = new PaymentOrderPO();
        BeanUtils.copyProperties(order, po);
        return po;
    }
    public List<PaymentOrder> toPaymentOrderList(List<PaymentOrderPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toPaymentOrder).collect(Collectors.toList());
    }

    public ElectricityRecord toElectricityRecord(ElectricityRecordPO po) {
        if (po == null) return null;
        ElectricityRecord r = new ElectricityRecord();
        BeanUtils.copyProperties(po, r);
        return r;
    }
    public ElectricityRecordPO toElectricityRecordPO(ElectricityRecord r) {
        if (r == null) return null;
        ElectricityRecordPO po = new ElectricityRecordPO();
        BeanUtils.copyProperties(r, po);
        return po;
    }
    public List<ElectricityRecord> toElectricityRecordList(List<ElectricityRecordPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toElectricityRecord).collect(Collectors.toList());
    }

    // ===== PaymentItem =====
    public PaymentItem toPaymentItem(PaymentItemPO po) {
        if (po == null) return null;
        PaymentItem item = new PaymentItem();
        BeanUtils.copyProperties(po, item);
        return item;
    }
    public PaymentItemPO toPaymentItemPO(PaymentItem item) {
        if (item == null) return null;
        PaymentItemPO po = new PaymentItemPO();
        BeanUtils.copyProperties(item, po);
        return po;
    }
    public List<PaymentItem> toPaymentItemList(List<PaymentItemPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toPaymentItem).collect(Collectors.toList());
    }
}
