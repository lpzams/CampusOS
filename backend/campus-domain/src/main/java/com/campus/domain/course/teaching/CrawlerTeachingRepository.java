package com.campus.domain.course.teaching;

import java.util.List;
import java.util.Map;

/** Relates crawler catalog courses, their claimed teacher account, and student selections. */
public interface CrawlerTeachingRepository {
    List<Map<String, Object>> findCoursesByTeacherId(Long teacherId);
    boolean ownsCourse(Long teacherId, String courseSourceId);
    List<Map<String, Object>> findStudents(Long teacherId, String courseSourceId);
    boolean markTeacherClaimed(Long teacherId);
}
