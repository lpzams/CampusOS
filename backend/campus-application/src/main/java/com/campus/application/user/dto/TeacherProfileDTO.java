package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 教师详细信息响应（2.9 PUT /user/profile/teacher）
 */
@Data
@Builder
public class TeacherProfileDTO {

    private Long userId;
    private String teacherId;
    private String title;
    private String researchDirection;
    private String office;
    private String introduction;
}
