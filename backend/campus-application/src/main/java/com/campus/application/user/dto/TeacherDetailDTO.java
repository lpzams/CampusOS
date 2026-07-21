package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 教师详细信息响应（2.5 GET /user/profile/teacher）
 */
@Data
@Builder
public class TeacherDetailDTO {

    private Long userId;
    private String teacherId;
    private String realName;
    private String gender;
    private String department;
    private String title;
    private String office;
    private String phone;
    private String email;
    private String researchArea;
    private String introduction;
}
