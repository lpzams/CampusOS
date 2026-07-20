package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class StudentSelectionDTO {
    private Long courseId;
    private String courseName;
    private Integer totalStudents;
    private List<StudentItemDTO> list;
}
