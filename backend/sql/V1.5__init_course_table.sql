-- ==================== 1. 课程表（仅课程定义，不含排课信息） ====================
CREATE TABLE t_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(20) COMMENT '课程编码',
    credit INT DEFAULT 0 COMMENT '学分',
    teacher_id BIGINT COMMENT '授课教师ID',
    teacher_name VARCHAR(50) COMMENT '教师姓名',
    semester VARCHAR(20) COMMENT '学期，如2024-2025-1',
    max_students INT DEFAULT 60 COMMENT '最大选课人数',
    color VARCHAR(20) COMMENT '课程颜色标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_teacher (teacher_id),
    INDEX idx_semester (semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ==================== 2. 教室表 ====================
CREATE TABLE t_classroom (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教室ID',
    building VARCHAR(50) NOT NULL COMMENT '教学楼名称',
    name VARCHAR(50) NOT NULL COMMENT '教室名称',
    floor INT DEFAULT 1 COMMENT '楼层',
    capacity INT DEFAULT 0 COMMENT '座位数',
    type INT DEFAULT 1 COMMENT '教室类型：1-普通 2-多媒体 3-智慧 4-实验室 5-机房',
    status INT DEFAULT 0 COMMENT '行政状态：0-空闲 1-占用 2-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_building_name (building, name),
    INDEX idx_building (building)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室表';

-- ==================== 3. 排课表（课程→教室+时间） ====================
CREATE TABLE t_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排课ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    day_of_week INT NOT NULL COMMENT '星期几：1-7（1=周一）',
    time_slot VARCHAR(50) NOT NULL COMMENT '时间段，如08:00-09:35',
    weeks VARCHAR(50) COMMENT '周次，如1-16周',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course (course_id),
    INDEX idx_classroom (classroom_id),
    INDEX idx_day_time (day_of_week, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课表';

-- ==================== 4. 选课表 ====================
CREATE TABLE t_course_enrollment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '选课ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    semester VARCHAR(20) COMMENT '学期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester),
    INDEX idx_student_semester (student_id, semester),
    INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课表';
