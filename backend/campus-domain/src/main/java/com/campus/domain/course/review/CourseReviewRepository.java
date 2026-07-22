package com.campus.domain.course.review;

import java.util.List;
import java.util.Map;

/** Read-only access to the anonymous course-review catalog imported by web-crawler. */
public interface CourseReviewRepository {
    List<Map<String, Object>> findCatalog(String keyword, int offset, int limit);

    List<Map<String, Object>> findReviews(String courseSourceId, int offset, int limit);
}
