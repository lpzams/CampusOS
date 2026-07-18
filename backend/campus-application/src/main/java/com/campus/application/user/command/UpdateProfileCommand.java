package com.campus.application.user.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改个人信息命令
 */
@Data
public class UpdateProfileCommand {

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 头像URL（通过上传接口获取） */
    private String avatar;
}
