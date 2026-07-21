package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 个人信息响应（2.1 GET /user/profile）
 */
@Data
@Builder
public class UserProfileDTO {

    private Long id;
    private String username;
    private String realName;
    private String gender;
    private String phone;
    private String email;
    private String avatar;
    private Integer userType;
    private String department;
    private String major;
    private String className;
    private String studentId;
    private String enrollmentYear;
    private Integer status;
    private String createdTime;
    private String updatedTime;
}
