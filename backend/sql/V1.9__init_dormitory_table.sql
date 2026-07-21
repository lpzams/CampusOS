-- ==================== 1. 宿舍房间表 ====================
CREATE TABLE t_dormitory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    dormitory_id VARCHAR(20) NOT NULL COMMENT '宿舍编号，如6-302',
    building VARCHAR(20) NOT NULL COMMENT '楼栋，如6栋',
    room VARCHAR(20) NOT NULL COMMENT '房间号，如302室',
    type VARCHAR(10) NOT NULL COMMENT '类型编码：FOUR/SIX/DOUBLE',
    type_name VARCHAR(20) COMMENT '类型名称：四人间/六人间/双人间',
    facilities JSON COMMENT '设施列表',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dormitory (dormitory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿舍房间表';

-- ==================== 2. 宿舍成员表 ====================
CREATE TABLE t_dormitory_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    dormitory_id VARCHAR(20) NOT NULL COMMENT '宿舍编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    is_room_leader TINYINT DEFAULT 0 COMMENT '是否舍长',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dormitory_user (dormitory_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿舍成员表';

-- ==================== 3. 宿舍公告表 ====================
CREATE TABLE t_dormitory_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content MEDIUMTEXT COMMENT '内容',
    type VARCHAR(20) COMMENT '类型：维修/安全/活动',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿舍公告表';

-- ==================== 4. 水电余额表 ====================
CREATE TABLE t_dormitory_utility (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    dormitory_id VARCHAR(20) NOT NULL COMMENT '宿舍编号',
    electricity_balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '电费余额',
    water_balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '水费余额',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dormitory (dormitory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿舍水电余额表';

-- ==================== 5. 水电使用记录表 ====================
CREATE TABLE t_dormitory_utility_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    dormitory_id VARCHAR(20) NOT NULL COMMENT '宿舍编号',
    record_month VARCHAR(7) NOT NULL COMMENT '月份，如2024-05',
    electricity_consumption DECIMAL(10,2) DEFAULT 0.00 COMMENT '用电量',
    electricity_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '电费',
    water_consumption DECIMAL(10,2) DEFAULT 0.00 COMMENT '用水量',
    water_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '水费',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dormitory_month (dormitory_id, record_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿舍水电使用记录表';
