-- CampusOS 多角色数据迁移：参考 CampusOS_a 的角色/档案模型。
SET NAMES utf8mb4;
USE campus_os;

CREATE TABLE IF NOT EXISTS t_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(200) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色字典';

INSERT INTO t_role (role_code, role_name, description) VALUES
  ('ROLE_STUDENT', '学生', '学生教务与校园生活服务'),
  ('ROLE_TEACHER', '教师', '教学与公共校园服务'),
  ('ROLE_ADMIN', '管理员', '系统管理与全量权限')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description);

-- 当前版本的用户主数据保存在 t_campus_record(record_type='user') 中；
-- 此表为角色审计与后续扩展保留，user_id 对应 t_campus_record.id。
CREATE TABLE IF NOT EXISTS t_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_role_user (user_id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES t_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联（user_id 对应通用用户记录）';

-- 学生/教师专属档案字段。当前应用继续从用户 JSON 中读取资料，表结构用于规范化迁移与报表。
CREATE TABLE IF NOT EXISTS t_user_profile (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  student_id VARCHAR(20) NULL,
  major VARCHAR(100) NULL,
  class_name VARCHAR(50) NULL,
  enrollment_year VARCHAR(10) NULL,
  teacher_id VARCHAR(20) NULL,
  title VARCHAR(50) NULL,
  research_direction VARCHAR(200) NULL,
  office VARCHAR(100) NULL,
  introduction VARCHAR(500) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_profile_user (user_id),
  UNIQUE KEY uk_user_profile_student (student_id),
  UNIQUE KEY uk_user_profile_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生和教师专属档案';

-- 将已有 JSON 用户按 userType 同步到角色关联表；重复执行不会重复插入。
INSERT IGNORE INTO t_user_role (user_id, role_id)
SELECT r.id, role_map.id
FROM t_campus_record r
JOIN t_role role_map ON role_map.role_code = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.record_data, '$.userType')) AS UNSIGNED)
  WHEN 2 THEN 'ROLE_TEACHER'
  WHEN 3 THEN 'ROLE_ADMIN'
  ELSE 'ROLE_STUDENT'
END
WHERE r.record_type = 'user';

-- 应用仍以 JSON 中的 userType 做鉴权，触发器确保规范化关联表始终同步。
DROP TRIGGER IF EXISTS trg_user_role_after_insert;
DROP TRIGGER IF EXISTS trg_user_role_after_update;
DELIMITER $$
CREATE TRIGGER trg_user_role_after_insert AFTER INSERT ON t_campus_record
FOR EACH ROW
BEGIN
  IF NEW.record_type = 'user' THEN
    INSERT IGNORE INTO t_user_role (user_id, role_id)
    SELECT NEW.id, id FROM t_role WHERE role_code = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(NEW.record_data, '$.userType')) AS UNSIGNED)
      WHEN 2 THEN 'ROLE_TEACHER' WHEN 3 THEN 'ROLE_ADMIN' ELSE 'ROLE_STUDENT' END;
  END IF;
END$$
CREATE TRIGGER trg_user_role_after_update AFTER UPDATE ON t_campus_record
FOR EACH ROW
BEGIN
  IF NEW.record_type = 'user' THEN
    DELETE FROM t_user_role WHERE user_id = NEW.id;
    INSERT INTO t_user_role (user_id, role_id)
    SELECT NEW.id, id FROM t_role WHERE role_code = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(NEW.record_data, '$.userType')) AS UNSIGNED)
      WHEN 2 THEN 'ROLE_TEACHER' WHEN 3 THEN 'ROLE_ADMIN' ELSE 'ROLE_STUDENT' END;
  END IF;
END$$
DELIMITER ;
