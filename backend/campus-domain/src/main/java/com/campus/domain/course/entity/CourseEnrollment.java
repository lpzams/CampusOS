package com.campus.domain.course.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseEnrollment {

    private Long id;
    private Long courseId;
    private Long studentId;
    private String semester;
    private LocalDateTime createTime;

    public static CourseEnrollment create(Long courseId, Long studentId, String semester) {
        CourseEnrollment e = new CourseEnrollment();
        e.setCourseId(courseId);
        e.setStudentId(studentId);
        e.setSemester(semester);
        e.setCreateTime(LocalDateTime.now());
        return e;
    }
}
