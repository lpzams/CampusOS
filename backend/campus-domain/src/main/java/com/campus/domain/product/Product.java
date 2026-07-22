package com.campus.domain.product;

import java.math.BigDecimal;

public class Product {
    public enum Status { ON_SALE, SOLD }

    private final Long sellerId;
    private final BigDecimal price;
    private Status status;

    public Product(Long sellerId, BigDecimal price, Status status) {
        if (sellerId == null) throw new IllegalArgumentException("卖家不能为空");
        if (price == null || price.signum() <= 0) throw new IllegalArgumentException("商品价格必须大于 0");
        this.sellerId = sellerId;
        this.price = price;
        this.status = status == null ? Status.ON_SALE : status;
    }

    public void orderBy(Long buyerId) {
        if (sellerId.equals(buyerId)) throw new IllegalStateException("不能购买自己发布的商品");
        if (status != Status.ON_SALE) throw new IllegalStateException("商品已不在售");
    }

    public Long sellerId() { return sellerId; }
    public BigDecimal price() { return price; }
    public Status status() { return status; }
}
