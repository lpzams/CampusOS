package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CourseListDTO {
    private Long id;
    private String name;
    private String courseCode;
    private Integer credit;
    private String teacherName;
    private String semester;
    private String classroom;
    private String timeSlot;
    private Integer studentCount;
    private Integer maxStudents;
    private Integer currentStudents;
    private Boolean isSelected;
}
