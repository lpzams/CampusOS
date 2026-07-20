-- ==================== 1. 考试安排表 ====================
CREATE TABLE t_exam (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考试ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(20) COMMENT '课程编码',
    exam_date DATE NOT NULL COMMENT '考试日期',
    exam_time VARCHAR(20) NOT NULL COMMENT '考试时间，如：14:00-16:00',
    building VARCHAR(50) COMMENT '教学楼',
    classroom VARCHAR(50) COMMENT '教室',
    exam_type VARCHAR(20) DEFAULT 'FINAL' COMMENT '考试类型：FINAL-期末 MIDTERM-期中 MAKEUP-补考',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待考试 COMPLETED-已考完',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_date (exam_date),
    INDEX idx_course (course_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';

-- ==================== 2. 考场表 ====================
CREATE TABLE t_exam_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考场ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    building VARCHAR(50) COMMENT '教学楼',
    classroom VARCHAR(50) NOT NULL COMMENT '教室',
    capacity INT DEFAULT 60 COMMENT '容量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考场表';

-- ==================== 3. 座位分配表 ====================
CREATE TABLE t_exam_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    room_id BIGINT NOT NULL COMMENT '考场ID',
    seat_number INT NOT NULL COMMENT '座位号',
    student_user_id BIGINT NOT NULL COMMENT '学生用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_exam_student (exam_id, student_user_id),
    INDEX idx_exam (exam_id),
    INDEX idx_room (room_id),
    INDEX idx_student (student_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试座位表';

-- ==================== 4. 考试提醒设置表 ====================
CREATE TABLE t_exam_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提醒ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-否 1-是',
    reminder_minutes INT DEFAULT 60 COMMENT '提前提醒分钟数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试提醒设置表';
