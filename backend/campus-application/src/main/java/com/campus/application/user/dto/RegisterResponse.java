package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private Long userId;
    private String username;
    private String realName;
    private Integer userType;
    private Integer status;
    private String message;
}