package com.campus.domain.product.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private String coverImage;
    private Integer categoryId;
    private Integer status;
    private Integer viewCount;
    private Integer favoriteCount;
    private Long sellerId;
    private String contactPhone;
    private String wechat;
    private String auditStatus;
    private LocalDateTime expectedAuditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 状态常量 =====
    public static final int STATUS_ON_SALE = 0;
    public static final int STATUS_SOLD = 1;
    public static final int STATUS_OFF_SHELF = 2;

    // ===== 审核状态常量 =====
    public static final String AUDIT_PENDING = "PENDING";
    public static final String AUDIT_APPROVED = "APPROVED";
    public static final String AUDIT_REJECTED = "REJECTED";

    // ===== 状态描述 =====
    public String getStatusDesc() {
        switch (status) {
            case STATUS_ON_SALE: return "在售";
            case STATUS_SOLD: return "已售";
            case STATUS_OFF_SHELF: return "已下架";
            default: return String.valueOf(status);
        }
    }

    public String getAuditStatusDesc() {
        if (AUDIT_PENDING.equals(auditStatus)) return "审核中";
        if (AUDIT_APPROVED.equals(auditStatus)) return "审核通过";
        if (AUDIT_REJECTED.equals(auditStatus)) return "审核拒绝";
        return auditStatus;
    }

    // ===== 工厂方法 =====

    public static Product create(Long sellerId, String title, BigDecimal price,
                                 String description, Integer categoryId,
                                 String coverImage, String contactPhone, String wechat) {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setTitle(title);
        p.setPrice(price);
        p.setDescription(description);
        p.setCategoryId(categoryId);
        p.setCoverImage(coverImage);
        p.setContactPhone(contactPhone);
        p.setWechat(wechat);
        p.setStatus(STATUS_ON_SALE);
        p.setViewCount(0);
        p.setFavoriteCount(0);
        p.setAuditStatus(AUDIT_PENDING);
        p.setExpectedAuditTime(LocalDateTime.now().plusDays(1));
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        return p;
    }

    // ===== 领域方法 =====

    /** 增加浏览次数 */
    public void incrementViewCount() {
        if (this.viewCount == null) this.viewCount = 0;
        this.viewCount++;
    }

    /** 下架 */
    public void offShelf() {
        if (this.status != null && this.status == STATUS_SOLD) {
            throw new IllegalStateException("已售商品不能下架");
        }
        this.status = STATUS_OFF_SHELF;
        this.updateTime = LocalDateTime.now();
    }

    /** 上架 */
    public void onShelf() {
        if (this.status != null && this.status != STATUS_OFF_SHELF) {
            throw new IllegalStateException("只有已下架商品才能重新上架");
        }
        this.status = STATUS_ON_SALE;
        this.updateTime = LocalDateTime.now();
    }

    /** 标记已售 */
    public void markSold() {
        if (this.status != null && this.status != STATUS_ON_SALE) {
            throw new IllegalStateException("只有在售商品才能标记为已售");
        }
        this.status = STATUS_SOLD;
        this.updateTime = LocalDateTime.now();
    }

    /** 审核通过 */
    public void approve() {
        this.auditStatus = AUDIT_APPROVED;
        this.updateTime = LocalDateTime.now();
    }

    /** 审核拒绝 */
    public void reject() {
        this.auditStatus = AUDIT_REJECTED;
        this.updateTime = LocalDateTime.now();
    }
}
