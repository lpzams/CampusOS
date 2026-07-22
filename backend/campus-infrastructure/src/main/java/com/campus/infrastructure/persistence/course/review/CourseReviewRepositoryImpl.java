package com.campus.infrastructure.persistence.course.review;

import com.campus.domain.course.review.CourseReviewRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CourseReviewRepositoryImpl implements CourseReviewRepository {
    private final JdbcTemplate jdbcTemplate;

    public CourseReviewRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> findCatalog(String keyword, int offset, int limit) {
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return jdbcTemplate.queryForList("""
                SELECT source_id AS sourceId, course_name AS courseName, professor, review_count AS reviewCount,
                       avg_overall AS avgOverall, avg_rate1 AS avgRate1, avg_rate2 AS avgRate2, avg_rate3 AS avgRate3
                FROM t_course_catalog
                WHERE course_name LIKE ? OR professor LIKE ?
                ORDER BY review_count DESC, avg_overall DESC, course_name ASC
                LIMIT ? OFFSET ?
                """, like, like, limit, offset);
    }

    @Override
    public List<Map<String, Object>> findReviews(String courseSourceId, int offset, int limit) {
        return jdbcTemplate.queryForList("""
                SELECT source_id AS sourceId, course_name AS courseName, professor, comment, overall,
                       rate1, rate2, rate3, tags, attendance, bird, homework, up_vote AS upVote,
                       down_vote AS downVote, source_created_at AS sourceCreatedAt
                FROM t_course_review
                WHERE course_source_id = ?
                ORDER BY source_created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """, courseSourceId, limit, offset);
    }
}
