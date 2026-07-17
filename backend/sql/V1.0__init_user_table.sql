-- ==================== 1. 用户主表 ====================
CREATE TABLE t_user (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                        username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（学号/工号）',
                        password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                        real_name VARCHAR(50) COMMENT '真实姓名',
                        phone VARCHAR(20) UNIQUE COMMENT '手机号',
                        email VARCHAR(100) UNIQUE COMMENT '邮箱',
                        avatar VARCHAR(500) COMMENT '头像URL',
                        gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
                        user_type TINYINT NOT NULL COMMENT '用户类型：1-学生 2-教师 3-管理员',
                        department VARCHAR(100) COMMENT '院系/部门',
                        status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-冻结 2-待审核 3-已注销',
                        is_verified TINYINT DEFAULT 0 COMMENT '实名认证：0-未认证 1-已认证',
                        last_login_time DATETIME COMMENT '最后登录时间',
                        last_login_ip VARCHAR(50) COMMENT '最后登录IP',
                        login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
                        lock_time DATETIME COMMENT '锁定时间',
                        created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

-- ==================== 2. 用户详细信息表 ====================
CREATE TABLE t_user_profile (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                                user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',

    -- 学生字段
                                student_id VARCHAR(20) COMMENT '学号',
                                major VARCHAR(100) COMMENT '专业',
                                class_name VARCHAR(50) COMMENT '班级',
                                enrollment_year VARCHAR(10) COMMENT '入学年份',

    -- 教师字段
                                teacher_id VARCHAR(20) COMMENT '工号',
                                title VARCHAR(50) COMMENT '职称',
                                research_direction VARCHAR(200) COMMENT '研究方向',

    -- 认证字段
                                id_card VARCHAR(18) COMMENT '身份证号（加密存储）',
                                id_card_front VARCHAR(500) COMMENT '身份证正面照',
                                id_card_back VARCHAR(500) COMMENT '身份证反面照',
                                student_card VARCHAR(500) COMMENT '学生证/教师证',
                                verify_status TINYINT DEFAULT 0 COMMENT '认证状态：0-未提交 1-审核中 2-已认证 3-已拒绝',
                                verify_reason VARCHAR(200) COMMENT '认证拒绝原因',
                                verified_time DATETIME COMMENT '认证通过时间',

                                created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户详细信息表';

-- ==================== 3. 角色表 ====================
CREATE TABLE t_role (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
                        role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
                        description VARCHAR(200) COMMENT '描述',
                        created_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

INSERT INTO t_role (role_code, role_name, description) VALUES
                                                           ('ROLE_STUDENT', '学生', '普通学生角色'),
                                                           ('ROLE_TEACHER', '教师', '教师角色'),
                                                           ('ROLE_ADMIN', '管理员', '系统管理员');

-- ==================== 4. 用户角色关联表 ====================
CREATE TABLE t_user_role (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id BIGINT NOT NULL COMMENT '用户ID',
                             role_id BIGINT NOT NULL COMMENT '角色ID',
                             created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES t_user(id),
                             FOREIGN KEY (role_id) REFERENCES t_role(id),
                             UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
