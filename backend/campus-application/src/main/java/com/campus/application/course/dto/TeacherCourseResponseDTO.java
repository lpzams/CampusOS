package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 5.4 教师授课列表响应
 */
@Data
@Builder
public class TeacherCourseResponseDTO {

    private Long teacherId;
    private String teacherName;
    private String department;
    private String semester;
    private List<TeacherCourseDTO> courses;
}
