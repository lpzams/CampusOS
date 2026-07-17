package com.campus.application.user.command;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterCommand {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度应在4-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @NotBlank(message = "院系/部门不能为空")
    private String department;

    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

    // ===== 学生专属字段 =====
    private String major;
    private String className;
    private String enrollmentYear;

    // ===== 教师专属字段 =====
    private String title;
    private String researchDirection;
}