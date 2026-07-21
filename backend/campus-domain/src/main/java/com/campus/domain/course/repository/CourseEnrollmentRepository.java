package com.campus.domain.course.repository;

import com.campus.domain.course.entity.CourseEnrollment;

import java.util.List;

public interface CourseEnrollmentRepository {

    void save(CourseEnrollment enrollment);

    void delete(Long courseId, Long studentId);

    boolean existsByCourseAndStudent(Long courseId, Long studentId);

    long countByCourseId(Long courseId);

    /** 查学生选课记录 */
    List<CourseEnrollment> findByStudentAndSemester(Long studentId, String semester);

    /** 按课程ID查所有选课学生ID */
    List<Long> findStudentIdsByCourseId(Long courseId);
}
