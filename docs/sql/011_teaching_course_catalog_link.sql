SET NAMES utf8mb4;
USE campus_os;

-- A formal teachingCourse is a semester-specific offering.  A crawler catalog
-- row is course-and-teacher review evidence.  Keep the two identifiers separate
-- and link them explicitly so enrollment, grades and reviews remain consistent.
CREATE TABLE IF NOT EXISTS t_teaching_course_catalog_link (
  teaching_course_id BIGINT NOT NULL,
  course_source_id VARCHAR(80) NOT NULL,
  link_status VARCHAR(30) NOT NULL DEFAULT 'ADMIN_CONFIRMED',
  linked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (teaching_course_id),
  KEY idx_teaching_catalog_source (course_source_id),
  CONSTRAINT fk_teaching_catalog_source FOREIGN KEY (course_source_id)
    REFERENCES t_course_catalog(source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='正式开课班与爬虫课程评价目录关联';

-- Only backfill an unambiguous exact course-name + teacher-name match.  A
-- similarly named course or teacher is intentionally left for admin confirmation.
INSERT IGNORE INTO t_teaching_course_catalog_link (teaching_course_id, course_source_id, link_status)
SELECT r.id, c.source_id, 'AUTO_EXACT'
FROM t_campus_record r
JOIN t_course_catalog c
  ON c.course_name = JSON_UNQUOTE(JSON_EXTRACT(r.record_data, '$.courseName'))
 AND c.professor = JSON_UNQUOTE(JSON_EXTRACT(r.record_data, '$.teacherName'))
WHERE r.record_type = 'teachingCourse'
  AND 1 = (
    SELECT COUNT(*) FROM t_course_catalog one_match
    WHERE one_match.course_name = JSON_UNQUOTE(JSON_EXTRACT(r.record_data, '$.courseName'))
      AND one_match.professor = JSON_UNQUOTE(JSON_EXTRACT(r.record_data, '$.teacherName'))
  );
