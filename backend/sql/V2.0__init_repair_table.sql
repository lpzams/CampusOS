-- ==================== 1. 报修单表 ====================
CREATE TABLE t_repair (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报修ID',
    user_id BIGINT NOT NULL COMMENT '提交用户ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：水电/设备/门窗/网络/其他',
    type_code VARCHAR(30) COMMENT '类型编码：WATER_ELECTRIC/EQUIPMENT/DOOR_WINDOW/NETWORK/OTHER',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description VARCHAR(1000) COMMENT '描述',
    images JSON COMMENT '图片URL数组',
    building VARCHAR(20) COMMENT '楼栋',
    room VARCHAR(20) COMMENT '房间号',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/CANCELLED',
    status_desc VARCHAR(100) COMMENT '状态描述',
    expected_time DATETIME COMMENT '预计完成时间',
    complete_time DATETIME COMMENT '实际完成时间',
    handler VARCHAR(50) COMMENT '维修人员',
    handler_phone VARCHAR(20) COMMENT '维修人员电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修单表';

-- ==================== 2. 报修进度表 ====================
CREATE TABLE t_repair_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    repair_id BIGINT NOT NULL COMMENT '报修ID',
    status VARCHAR(20) COMMENT '状态',
    content VARCHAR(500) COMMENT '进度内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_repair (repair_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修进度表';

-- ==================== 3. 评价表 ====================
CREATE TABLE t_repair_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    repair_id BIGINT NOT NULL COMMENT '报修ID',
    user_id BIGINT NOT NULL COMMENT '评价用户ID',
    score INT NOT NULL COMMENT '评分1-5',
    content VARCHAR(500) COMMENT '评价内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_repair_user (repair_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修评价表';
