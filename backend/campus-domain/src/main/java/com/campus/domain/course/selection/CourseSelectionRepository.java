package com.campus.domain.course.selection;

import java.util.List;
import java.util.Map;

public interface CourseSelectionRepository {
    boolean catalogExists(String courseSourceId);
    boolean select(Long userId, String courseSourceId);
    boolean cancel(Long userId, String courseSourceId);
    List<Map<String, Object>> findByUserId(Long userId);
}
