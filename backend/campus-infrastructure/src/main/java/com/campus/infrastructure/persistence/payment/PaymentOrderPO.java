package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_payment_order")
public class PaymentOrderPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("payment_id") private Long paymentId;
    @TableField("user_id") private Long userId;
    @TableField("order_no") private String orderNo;
    private BigDecimal amount;
    @TableField("pay_method") private String payMethod;
    private String status;
    @TableField("pay_url") private String payUrl;
    @TableField("qr_code") private String qrCode;
    @TableField("pay_time") private LocalDateTime payTime;
    @TableField("expire_time") private LocalDateTime expireTime;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
}
