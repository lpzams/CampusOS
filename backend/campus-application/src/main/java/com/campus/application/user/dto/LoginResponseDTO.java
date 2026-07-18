package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginResponseDTO {

    /** JWT Token */
    private String token;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 真实姓名 */
    private String realName;
    /** 用户类型：1-学生 2-教师 3-管理员 */
    private Integer userType;
    /** 头像URL */
    private String avatar;
    /** Token 过期时间（毫秒） */
    private Long expiresIn;
    /** 是否首次登录（微信授权自动注册时为 true） */
    private Boolean isFirstLogin;
}
