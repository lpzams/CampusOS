package com.campus.common.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户信息（存入 ThreadLocal / Redis）
 */
@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private Integer userType;
    private String avatar;

    public static LoginUser of(Long userId, String username, String realName,
                               Integer userType, String avatar) {
        LoginUser user = new LoginUser();
        user.setUserId(userId);
        user.setUsername(username);
        user.setRealName(realName);
        user.setUserType(userType);
        user.setAvatar(avatar);
        return user;
    }
}
