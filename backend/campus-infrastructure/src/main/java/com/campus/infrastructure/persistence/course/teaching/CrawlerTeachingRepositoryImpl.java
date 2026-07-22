package com.campus.infrastructure.persistence.course.teaching;

import com.campus.domain.course.teaching.CrawlerTeachingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CrawlerTeachingRepositoryImpl implements CrawlerTeachingRepository {
    private final JdbcTemplate jdbcTemplate;

    public CrawlerTeachingRepositoryImpl(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Override
    public List<Map<String, Object>> findCoursesByTeacherId(Long teacherId) {
        return jdbcTemplate.queryForList("""
                SELECT c.source_id AS sourceId, c.course_name AS courseName, c.professor,
                       c.review_count AS reviewCount, c.avg_overall AS avgOverall,
                       a.assignment_status AS assignmentStatus, COUNT(s.id) AS selectedStudentCount
                FROM t_course_teacher_assignment a
                JOIN t_course_catalog c ON c.source_id = a.course_source_id
                LEFT JOIN t_course_selection s ON s.course_source_id = a.course_source_id
                WHERE a.teacher_user_id = ?
                GROUP BY c.source_id, c.course_name, c.professor, c.review_count, c.avg_overall, a.assignment_status
                ORDER BY c.course_name
                """, teacherId);
    }

    @Override
    public boolean ownsCourse(Long teacherId, String courseSourceId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM t_course_teacher_assignment
                WHERE teacher_user_id = ? AND course_source_id = ?
                """, Integer.class, teacherId, courseSourceId);
        return count != null && count > 0;
    }

    @Override
    public List<Map<String, Object>> findStudents(Long teacherId, String courseSourceId) {
        return jdbcTemplate.queryForList("""
                SELECT s.user_id AS studentId,
                       JSON_UNQUOTE(JSON_EXTRACT(u.record_data, '$.username')) AS username,
                       JSON_UNQUOTE(JSON_EXTRACT(u.record_data, '$.realName')) AS realName,
                       JSON_UNQUOTE(JSON_EXTRACT(u.record_data, '$.studentId')) AS studentNumber,
                       s.selected_at AS selectedAt
                FROM t_course_selection s
                JOIN t_campus_record u ON u.id = s.user_id AND u.record_type = 'user'
                JOIN t_course_teacher_assignment a ON a.course_source_id = s.course_source_id
                WHERE a.teacher_user_id = ? AND s.course_source_id = ?
                ORDER BY s.selected_at DESC
                """, teacherId, courseSourceId);
    }

    @Override
    public boolean markTeacherClaimed(Long teacherId) {
        return jdbcTemplate.update("""
                UPDATE t_crawler_teacher_identity
                SET identity_status = 'CLAIMED'
                WHERE account_user_id = ?
                """, teacherId) > 0;
    }
}
