package com.campus.application.user.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信授权登录命令
 */
@Data
public class WechatLoginCommand {

    /** 微信授权临时 code */
    @NotBlank(message = "微信授权code不能为空")
    private String code;

    /** 微信用户信息 */
    private WechatUserInfo userInfo;

    @Data
    public static class WechatUserInfo {
        /** 微信昵称 */
        private String nickName;
        /** 微信头像URL */
        private String avatarUrl;
    }
}
