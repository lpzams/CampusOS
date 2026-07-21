package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 学生详细信息响应（2.8 PUT /user/profile/student）
 */
@Data
@Builder
public class StudentProfileDTO {

    private Long userId;
    private String studentId;
    private String major;
    private String className;
    private String enrollmentYear;
    private String dormitory;
    private String advisor;
}
