package com.campus.application.user.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 实名认证命令
 */
@Data
public class VerifyCommand {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    /** 身份证正面照 URL */
    @NotBlank(message = "身份证正面照不能为空")
    private String idCardFront;

    /** 身份证反面照 URL */
    @NotBlank(message = "身份证反面照不能为空")
    private String idCardBack;
}
