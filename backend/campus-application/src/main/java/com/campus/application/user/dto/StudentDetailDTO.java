package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 学生详细信息响应（2.4 GET /user/profile/student）
 */
@Data
@Builder
public class StudentDetailDTO {

    private Long userId;
    private String studentId;
    private String realName;
    private String gender;
    private String department;
    private String major;
    private String className;
    private String enrollmentYear;
    private String dormitory;
    private String advisor;
}
