package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TeacherCourseDTO {
    private Long id;
    private String name;
    private String courseCode;
    private Integer credit;
    private String classroom;
    private String timeSlot;
    private Integer students;
    private String schedule;
}
