package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class StudentItemDTO {
    private Long userId;
    private String studentId;
    private String realName;
    private String department;
    private String major;
    private String className;
}
