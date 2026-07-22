SET NAMES utf8mb4;
USE campus_os;

-- Formal teaching schedules, enrollments and teacher-entered scores are stored in
-- t_campus_record as teachingCourse, courseEnrollment and score records. These
-- generated columns preserve that data model while indexing the relation keys.
DELIMITER $$
DROP PROCEDURE IF EXISTS apply_teaching_schedule_indexes $$
CREATE PROCEDURE apply_teaching_schedule_indexes()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND column_name = 'teaching_teacher_id') THEN
    ALTER TABLE t_campus_record ADD COLUMN teaching_teacher_id BIGINT GENERATED ALWAYS AS (CASE WHEN record_type = 'teachingCourse' THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(record_data, '$.teacherId')) AS UNSIGNED) END) STORED;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND column_name = 'enrollment_student_id') THEN
    ALTER TABLE t_campus_record ADD COLUMN enrollment_student_id BIGINT GENERATED ALWAYS AS (CASE WHEN record_type = 'courseEnrollment' THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(record_data, '$.studentId')) AS UNSIGNED) END) STORED;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND column_name = 'enrollment_course_id') THEN
    ALTER TABLE t_campus_record ADD COLUMN enrollment_course_id BIGINT GENERATED ALWAYS AS (CASE WHEN record_type = 'courseEnrollment' THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(record_data, '$.courseId')) AS UNSIGNED) END) STORED;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND column_name = 'score_student_id') THEN
    ALTER TABLE t_campus_record ADD COLUMN score_student_id BIGINT GENERATED ALWAYS AS (CASE WHEN record_type = 'score' THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(record_data, '$.userId')) AS UNSIGNED) END) STORED;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND column_name = 'score_course_id') THEN
    ALTER TABLE t_campus_record ADD COLUMN score_course_id BIGINT GENERATED ALWAYS AS (CASE WHEN record_type = 'score' THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(record_data, '$.courseId')) AS UNSIGNED) END) STORED;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND index_name = 'idx_teaching_course_teacher') THEN
    CREATE INDEX idx_teaching_course_teacher ON t_campus_record (record_type, teaching_teacher_id);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND index_name = 'idx_course_enrollment_student') THEN
    CREATE INDEX idx_course_enrollment_student ON t_campus_record (record_type, enrollment_student_id);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND index_name = 'uk_course_enrollment_student_course') THEN
    CREATE UNIQUE INDEX uk_course_enrollment_student_course ON t_campus_record (record_type, enrollment_student_id, enrollment_course_id);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 't_campus_record' AND index_name = 'uk_score_student_course') THEN
    CREATE UNIQUE INDEX uk_score_student_course ON t_campus_record (record_type, score_student_id, score_course_id);
  END IF;
END $$
CALL apply_teaching_schedule_indexes() $$
DROP PROCEDURE apply_teaching_schedule_indexes $$
DELIMITER ;
