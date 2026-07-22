SET NAMES utf8mb4;
USE campus_os;

-- 学生从爬虫导入的课程-教师目录中选择的意向课。
-- 课程目录没有开课容量、时间或官方教务身份信息，因此此表不替代学校教务系统的正式选课记录。
CREATE TABLE IF NOT EXISTS t_course_selection (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_source_id VARCHAR(80) NOT NULL,
  selected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_course_selection (user_id, course_source_id),
  KEY idx_course_selection_user (user_id),
  KEY idx_course_selection_source (course_source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基于课程评价目录的学生选课意向';
