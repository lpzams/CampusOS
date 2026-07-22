SET NAMES utf8mb4;
USE campus_os;

-- 角色权限字典。现有鉴权继续使用 userType（学生/教师/管理员），此表为可审计的权限模型。
CREATE TABLE IF NOT EXISTS t_permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  permission_code VARCHAR(80) NOT NULL,
  permission_name VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限字典';

CREATE TABLE IF NOT EXISTS t_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES t_role(id),
  CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES t_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

INSERT INTO t_role (role_code, role_name, description) VALUES
  ('ROLE_ACADEMIC_ADMIN', '教务管理员', '课程目录、教师认领和选课记录管理')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description);

INSERT INTO t_permission (permission_code, permission_name, description) VALUES
  ('COURSE_CATALOG_READ', '查看课程目录', '查看爬虫同步的课程教师目录'),
  ('COURSE_REVIEW_READ', '查看课程评价', '查看已审核匿名课程评价'),
  ('COURSE_SELECTION_SELF', '维护本人选课', '选择和取消本人课程清单'),
  ('TEACHING_COURSE_READ', '查看本人授课', '查看已关联到本人的课程目录'),
  ('TEACHING_STUDENT_READ', '查看选课学生', '查看本人课程的学生清单'),
  ('TEACHING_GRADE_WRITE', '录入课程成绩', '录入本人授课学生成绩'),
  ('ACADEMIC_COURSE_MANAGE', '管理课程映射', '管理课程、教师与学期的正式映射'),
  ('ACADEMIC_TEACHER_CLAIM', '认领教师身份', '审核并激活爬虫教师账户'),
  ('ACADEMIC_SELECTION_READ', '查看全量选课', '查看全体学生的课程选择记录'),
  ('SYSTEM_USER_MANAGE', '管理用户', '管理系统账户、状态和角色')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name), description = VALUES(description);

INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM t_role r JOIN t_permission p
WHERE (r.role_code = 'ROLE_STUDENT' AND p.permission_code IN ('COURSE_CATALOG_READ','COURSE_REVIEW_READ','COURSE_SELECTION_SELF'))
   OR (r.role_code = 'ROLE_TEACHER' AND p.permission_code IN ('COURSE_CATALOG_READ','COURSE_REVIEW_READ','TEACHING_COURSE_READ','TEACHING_STUDENT_READ','TEACHING_GRADE_WRITE'))
   OR (r.role_code = 'ROLE_ACADEMIC_ADMIN' AND p.permission_code IN ('COURSE_CATALOG_READ','COURSE_REVIEW_READ','ACADEMIC_COURSE_MANAGE','ACADEMIC_TEACHER_CLAIM','ACADEMIC_SELECTION_READ'))
   OR (r.role_code = 'ROLE_ADMIN');

-- 爬虫只提供教师姓名，不能验证真实身份。姓名经来源命名空间哈希后作为可重复生成的目录主键。
CREATE TABLE IF NOT EXISTS t_crawler_teacher_identity (
  teacher_key CHAR(64) NOT NULL,
  professor_name VARCHAR(100) NOT NULL,
  account_user_id BIGINT NULL,
  identity_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CLAIM',
  source_name VARCHAR(50) NOT NULL DEFAULT 'huoshui',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (teacher_key),
  UNIQUE KEY uk_crawler_teacher_name (source_name, professor_name),
  UNIQUE KEY uk_crawler_teacher_user (account_user_id),
  CONSTRAINT fk_crawler_teacher_user FOREIGN KEY (account_user_id) REFERENCES t_campus_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待认领的爬虫教师身份目录';

CREATE TABLE IF NOT EXISTS t_course_teacher_assignment (
  course_source_id VARCHAR(80) NOT NULL,
  teacher_key CHAR(64) NOT NULL,
  teacher_user_id BIGINT NOT NULL,
  assignment_status VARCHAR(30) NOT NULL DEFAULT 'REFERENCE_ONLY',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (course_source_id),
  KEY idx_course_teacher_user (teacher_user_id),
  KEY idx_course_teacher_key (teacher_key),
  CONSTRAINT fk_assignment_catalog FOREIGN KEY (course_source_id) REFERENCES t_course_catalog(source_id),
  CONSTRAINT fk_assignment_identity FOREIGN KEY (teacher_key) REFERENCES t_crawler_teacher_identity(teacher_key),
  CONSTRAINT fk_assignment_user FOREIGN KEY (teacher_user_id) REFERENCES t_campus_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫课程目录与教师账户映射';

INSERT IGNORE INTO t_crawler_teacher_identity (teacher_key, professor_name)
SELECT DISTINCT SHA2(CONCAT('huoshui:', TRIM(professor)), 256), TRIM(professor)
FROM t_course_catalog
WHERE professor IS NOT NULL AND TRIM(professor) <> '';

-- 创建不可登录的教师占位账户。账号只有在身份认领、设置独立密码并启用后才能使用。
-- passwordHash 是有效 bcrypt 格式的锁定占位值；status=1 与 mustResetPassword 双重阻止登录。
INSERT INTO t_campus_record (record_type, record_data)
SELECT 'user', JSON_OBJECT(
  'username', CONCAT('crawler_teacher_', LOWER(SUBSTRING(t.teacher_key, 1, 16))),
  'passwordHash', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'realName', t.professor_name,
  'userType', 2,
  'roleCode', 'ROLE_TEACHER',
  'status', 1,
  'statusRemark', '课程评价数据导入后待身份认领',
  'mustResetPassword', true,
  'accountSource', 'course_review_catalog',
  'teacherKey', t.teacher_key,
  'identityStatus', 'PENDING_CLAIM',
  'createdTime', DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%s')
)
FROM t_crawler_teacher_identity t
LEFT JOIN t_campus_record u ON u.record_type = 'user'
  AND JSON_UNQUOTE(JSON_EXTRACT(u.record_data, '$.teacherKey')) = t.teacher_key
WHERE u.id IS NULL;

UPDATE t_crawler_teacher_identity t
JOIN t_campus_record u ON u.record_type = 'user'
  AND JSON_UNQUOTE(JSON_EXTRACT(u.record_data, '$.teacherKey')) = t.teacher_key
SET t.account_user_id = u.id;

INSERT IGNORE INTO t_course_teacher_assignment (course_source_id, teacher_key, teacher_user_id)
SELECT c.source_id, t.teacher_key, t.account_user_id
FROM t_course_catalog c
JOIN t_crawler_teacher_identity t
  ON t.teacher_key = SHA2(CONCAT('huoshui:', TRIM(c.professor)), 256)
WHERE t.account_user_id IS NOT NULL;
