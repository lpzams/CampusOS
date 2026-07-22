package com.campus.infrastructure.persistence.course.teaching;

import com.campus.domain.course.teaching.TeachingCatalogLinkRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TeachingCatalogLinkRepositoryImpl implements TeachingCatalogLinkRepository {
    private final JdbcTemplate jdbcTemplate;

    public TeachingCatalogLinkRepositoryImpl(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Override public Map<String, Object> findCatalogCourse(String courseSourceId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT source_id AS sourceId, course_name AS courseName, professor, review_count AS reviewCount,
                       avg_overall AS avgOverall, avg_rate1 AS avgRate1, avg_rate2 AS avgRate2, avg_rate3 AS avgRate3
                FROM t_course_catalog WHERE source_id = ?
                """, courseSourceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override public Map<Long, Map<String, Object>> findLinks(Collection<Long> teachingCourseIds) {
        if (teachingCourseIds.isEmpty()) return Map.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(teachingCourseIds.size(), "?"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT l.teaching_course_id AS teachingCourseId, l.course_source_id AS catalogSourceId,
                       l.link_status AS linkStatus, c.course_name AS catalogCourseName, c.professor,
                       c.review_count AS reviewCount, c.avg_overall AS avgOverall,
                       c.avg_rate1 AS avgRate1, c.avg_rate2 AS avgRate2, c.avg_rate3 AS avgRate3
                FROM t_teaching_course_catalog_link l
                JOIN t_course_catalog c ON c.source_id = l.course_source_id
                WHERE l.teaching_course_id IN (%s)
                """.formatted(placeholders), teachingCourseIds.toArray());
        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) result.put(((Number) row.get("teachingCourseId")).longValue(), row);
        return result;
    }

    @Override public void link(Long teachingCourseId, String courseSourceId) {
        jdbcTemplate.update("""
                INSERT INTO t_teaching_course_catalog_link (teaching_course_id, course_source_id, link_status)
                VALUES (?, ?, 'ADMIN_CONFIRMED')
                ON DUPLICATE KEY UPDATE course_source_id = VALUES(course_source_id),
                    link_status = 'ADMIN_CONFIRMED', linked_at = CURRENT_TIMESTAMP
                """, teachingCourseId, courseSourceId);
    }
    @Override public void unlink(Long teachingCourseId) {
        jdbcTemplate.update("DELETE FROM t_teaching_course_catalog_link WHERE teaching_course_id = ?", teachingCourseId);
    }
}
