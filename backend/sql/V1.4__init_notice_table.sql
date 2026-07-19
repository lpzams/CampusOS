-- ==================== 1. 公告表 ====================
CREATE TABLE t_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content MEDIUMTEXT COMMENT '内容（富文本HTML）',
    type VARCHAR(20) NOT NULL COMMENT '公告类型：SCHOOL-学校公告 DEPT-院系公告',
    department VARCHAR(100) COMMENT '发布部门/院系',
    summary VARCHAR(500) COMMENT '摘要',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    read_count INT DEFAULT 0 COMMENT '阅读次数',
    deadline DATETIME COMMENT '截止时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_type (type),
    INDEX idx_deadline (deadline),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ==================== 2. 公告附件表 ====================
CREATE TABLE t_notice_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    notice_id BIGINT NOT NULL COMMENT '公告ID',
    name VARCHAR(200) NOT NULL COMMENT '附件名称',
    url VARCHAR(500) NOT NULL COMMENT '附件URL',
    size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_notice_id (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告附件表';

-- ==================== 3. 公告已读记录表 ====================
CREATE TABLE t_notice_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notice_id BIGINT NOT NULL COMMENT '公告ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY uk_user_notice (user_id, notice_id),
    INDEX idx_user (user_id),
    INDEX idx_notice (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';
