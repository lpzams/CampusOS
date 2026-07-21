-- ==================== 1. 缴费项目模板表（管理员创建） ====================
CREATE TABLE t_payment_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    type VARCHAR(30) NOT NULL COMMENT '类型编码：TUITION/DORMITORY/BOOK',
    type_name VARCHAR(50) NOT NULL COMMENT '类型名称：学费/住宿费/教材费',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    deadline DATE COMMENT '截止日期',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-进行中 CLOSED-已关闭',
    description VARCHAR(200) COMMENT '描述',
    target_users JSON COMMENT '目标用户类型：["STUDENT","TEACHER"]',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_status (status),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费项目模板表';

-- ==================== 2. 个人缴费记录表（从模板生成） ====================
CREATE TABLE t_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缴费ID',
    item_id BIGINT COMMENT '缴费项目ID（关联t_payment_item）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '缴费类型',
    type_code VARCHAR(30) NOT NULL COMMENT '类型编码',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    deadline DATE COMMENT '截止日期',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待缴费 PAID-已缴费 OVERDUE-已逾期',
    description VARCHAR(200) COMMENT '描述',
    late_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '滞纳金',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_item (item_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人缴费记录表';

-- ==================== 3. 缴费订单表 ====================
CREATE TABLE t_payment_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    payment_id BIGINT NOT NULL COMMENT '缴费记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_method VARCHAR(20) NOT NULL COMMENT '支付方式：WECHAT/ALIPAY',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待支付 PAID-已支付 FAILED-失败',
    pay_url VARCHAR(500) COMMENT '支付链接',
    qr_code VARCHAR(500) COMMENT '二维码URL',
    pay_time DATETIME COMMENT '支付时间',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user (user_id),
    INDEX idx_payment (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费订单表';

-- ==================== 4. 电费余额表 ====================
CREATE TABLE t_electricity_balance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    dormitory_id VARCHAR(20) NOT NULL COMMENT '宿舍ID',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '当前余额',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dormitory (dormitory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电费余额表';

-- ==================== 5. 电费充值记录表 ====================
CREATE TABLE t_electricity_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dormitory_id VARCHAR(20) NOT NULL COMMENT '宿舍ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    old_balance DECIMAL(10,2) COMMENT '充值前余额',
    new_balance DECIMAL(10,2) COMMENT '充值后余额',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    pay_method VARCHAR(20) COMMENT '支付方式',
    pay_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_dormitory (dormitory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电费充值记录表';
