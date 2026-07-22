-- 004 课程目录与课程评价表结构
-- 全量评价由 web-crawler/scripts/export_mysql_seed.py 从本地爬取结果生成，
-- 生成文件 docs/sql/004_course_reviews.sql 不进入版本库，避免二次公开评价正文。
SET NAMES utf8mb4;
USE campus_os;

CREATE TABLE IF NOT EXISTS t_course_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_id VARCHAR(80) NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  professor VARCHAR(100) NOT NULL,
  review_count INT NOT NULL DEFAULT 0,
  avg_overall DECIMAL(5,2) NULL,
  avg_rate1 DECIMAL(5,2) NULL,
  avg_rate2 DECIMAL(5,2) NULL,
  avg_rate3 DECIMAL(5,2) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_source (source_id),
  KEY idx_course_name (course_name),
  KEY idx_professor (professor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程-教师评价聚合';

CREATE TABLE IF NOT EXISTS t_course_review (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_id VARCHAR(80) NOT NULL,
  course_source_id VARCHAR(80) NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  professor VARCHAR(100) NOT NULL,
  comment TEXT NOT NULL,
  overall TINYINT NULL,
  rate1 TINYINT NULL,
  rate2 TINYINT NULL,
  rate3 TINYINT NULL,
  tags VARCHAR(500) NULL,
  attendance VARCHAR(50) NULL,
  bird VARCHAR(50) NULL,
  homework VARCHAR(50) NULL,
  up_vote INT NOT NULL DEFAULT 0,
  down_vote INT NOT NULL DEFAULT 0,
  source_created_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_review_source (source_id),
  KEY idx_review_course (course_source_id),
  KEY idx_review_overall (overall)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匿名课程评价';
