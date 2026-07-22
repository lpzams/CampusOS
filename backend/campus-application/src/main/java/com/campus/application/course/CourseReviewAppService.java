package com.campus.application.course;

import com.campus.domain.course.review.CourseReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourseReviewAppService {
    private static final int MAX_PAGE_SIZE = 100;
    private final CourseReviewRepository repository;

    public CourseReviewAppService(CourseReviewRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> catalog(String keyword, int page, int size) {
        return repository.findCatalog(keyword, offset(page, size), pageSize(size));
    }

    public List<Map<String, Object>> reviews(String courseSourceId, int page, int size) {
        return repository.findReviews(courseSourceId, offset(page, size), pageSize(size));
    }

    private int pageSize(int size) { return Math.min(Math.max(size, 1), MAX_PAGE_SIZE); }
    private int offset(int page, int size) { return (Math.max(page, 1) - 1) * pageSize(size); }
}
