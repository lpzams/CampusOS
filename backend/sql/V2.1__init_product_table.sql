-- ==================== 1. 商品分类表 ====================
CREATE TABLE t_product_category (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 预置分类数据
INSERT INTO t_product_category (id, name) VALUES
(1, '电子产品'),
(2, '书籍教材'),
(3, '学习资料'),
(4, '生活用品'),
(5, '运动器材'),
(6, '服饰鞋包'),
(7, '其他');

-- ==================== 2. 商品表 ====================
CREATE TABLE t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    title VARCHAR(200) NOT NULL COMMENT '商品标题',
    price DECIMAL(10, 2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10, 2) COMMENT '原价',
    description VARCHAR(2000) COMMENT '商品描述',
    cover_image VARCHAR(500) COMMENT '封面图',
    category_id INT COMMENT '分类ID',
    status INT DEFAULT 0 COMMENT '状态：0-在售 1-已售 2-已下架',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    seller_id BIGINT NOT NULL COMMENT '卖家用户ID',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    wechat VARCHAR(50) COMMENT '微信号',
    audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-审核中 APPROVED-审核通过 REJECTED-审核拒绝',
    expected_audit_time DATETIME COMMENT '预期审核完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_seller (seller_id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品表';

-- ==================== 3. 商品图片表 ====================
CREATE TABLE t_product_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- ==================== 4. 商品收藏表 ====================
CREATE TABLE t_product_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user (user_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- ==================== 5. 商品订单表 ====================
CREATE TABLE t_product_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    order_id VARCHAR(30) NOT NULL COMMENT '订单编号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    buyer_id BIGINT NOT NULL COMMENT '买家用户ID',
    seller_id BIGINT NOT NULL COMMENT '卖家用户ID',
    price DECIMAL(10, 2) NOT NULL COMMENT '成交价',
    message VARCHAR(500) COMMENT '买家留言',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待确认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_id (order_id),
    INDEX idx_buyer (buyer_id),
    INDEX idx_seller (seller_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订单表';
