- ==================== 2. 成绩表 ====================
CREATE TABLE t_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_user_id BIGINT NOT NULL COMMENT '学生用户ID',
    score INT NOT NULL COMMENT '成绩（0-100）',
    grade VARCHAR(10) COMMENT '等级：优秀/良好/中等/及格/不及格',
    type VARCHAR(10) NOT NULL DEFAULT 'EXAM' COMMENT '类型：EXAM-考试 USUAL-平时',
    exam_time DATE COMMENT '考试时间',
    reason VARCHAR(500) COMMENT '修改原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    UNIQUE KEY uk_course_student_type (course_id, student_user_id, type),
    INDEX idx_course (course_id),
    INDEX idx_student (student_user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';
