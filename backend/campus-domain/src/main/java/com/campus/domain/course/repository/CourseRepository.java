package com.campus.domain.course.repository;

import com.campus.domain.course.entity.Course;

import java.util.List;

public interface CourseRepository {

    Course findById(Long id);

    void save(Course course);

    void update(Course course);

    void delete(Long id);

    List<Course> findPage(String semester, String keyword, int offset, int size);

    long count(String semester, String keyword);

    List<Course> findByTeacherAndSemester(Long teacherId, String semester);

    /** 查可选课程（已排课的课程） */
    List<Course> findAvailable(String semester, String keyword, int offset, int size);

    long countAvailable(String semester, String keyword);

    boolean existsByCourseCode(String courseCode);

    boolean existsByCourseCodeExcludingId(String courseCode, Long excludeId);
}
