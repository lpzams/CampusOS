package com.campus.infrastructure.persistence.course.selection;

import com.campus.domain.course.selection.CourseSelectionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CourseSelectionRepositoryImpl implements CourseSelectionRepository {
    private final JdbcTemplate jdbcTemplate;

    public CourseSelectionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean catalogExists(String courseSourceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_course_catalog WHERE source_id = ?", Integer.class, courseSourceId);
        return count != null && count > 0;
    }

    @Override
    public boolean select(Long userId, String courseSourceId) {
        return jdbcTemplate.update("INSERT IGNORE INTO t_course_selection(user_id, course_source_id) VALUES (?, ?)",
                userId, courseSourceId) > 0;
    }

    @Override
    public boolean cancel(Long userId, String courseSourceId) {
        return jdbcTemplate.update("DELETE FROM t_course_selection WHERE user_id = ? AND course_source_id = ?",
                userId, courseSourceId) > 0;
    }

    @Override
    public List<Map<String, Object>> findByUserId(Long userId) {
        return jdbcTemplate.queryForList("""
                SELECT c.source_id AS sourceId, c.course_name AS courseName, c.professor,
                       c.review_count AS reviewCount, c.avg_overall AS avgOverall, s.selected_at AS selectedAt
                FROM t_course_selection s
                JOIN t_course_catalog c ON c.source_id = s.course_source_id
                WHERE s.user_id = ?
                ORDER BY s.selected_at DESC
                """, userId);
    }
}
