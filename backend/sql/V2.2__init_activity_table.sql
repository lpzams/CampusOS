-- ==================== 1. 活动表 ====================
CREATE TABLE t_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    category VARCHAR(20) NOT NULL COMMENT '分类：SPORTS/CULTURE/ACADEMIC/VOLUNTEER',
    cover_image VARCHAR(500) COMMENT '封面图URL',
    content TEXT COMMENT '活动详情（HTML）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    location VARCHAR(200) NOT NULL COMMENT '活动地点',
    max_participants INT NOT NULL COMMENT '最大参与人数',
    current_participants INT DEFAULT 0 COMMENT '当前报名人数',
    organizer VARCHAR(100) COMMENT '主办方',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status VARCHAR(20) DEFAULT 'UPCOMING' COMMENT '状态：UPCOMING/ONGOING/ENDED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园活动表';

-- ==================== 2. 活动报名表 ====================
CREATE TABLE t_activity_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status VARCHAR(20) DEFAULT 'REGISTERED' COMMENT '状态：REGISTERED-已报名 CANCELLED-已取消',
    checkin_status TINYINT DEFAULT 0 COMMENT '签到状态：0-未签到 1-已签到',
    checkin_code VARCHAR(10) COMMENT '签到码',
    checkin_time DATETIME COMMENT '签到时间',
    remark VARCHAR(500) COMMENT '报名备注',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_activity (activity_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';
