-- ==================== 1. 校园卡表 ====================
CREATE TABLE t_card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    card_id VARCHAR(20) NOT NULL COMMENT '卡号，如C20240001',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '持卡人姓名',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL-正常 LOST-挂失 FROZEN-冻结',
    expire_time DATE COMMENT '有效期',
    last_recharge_time DATETIME COMMENT '最近充值时间',
    last_consume_time DATETIME COMMENT '最近消费时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_card_id (card_id),
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园卡表';

-- ==================== 2. 校园卡交易记录表 ====================
CREATE TABLE t_card_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    card_id VARCHAR(20) NOT NULL COMMENT '卡号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：CONSUME-消费 RECHARGE-充值',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额（消费为负，充值为正）',
    balance DECIMAL(10,2) COMMENT '交易后余额',
    merchant VARCHAR(100) COMMENT '商户/充值渠道',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_card (card_id),
    INDEX idx_user (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园卡交易记录表';
