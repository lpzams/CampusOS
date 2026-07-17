package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class UserPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @TableField("password_hash")
    private String passwordHash;
    @TableField("real_name")
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    @TableField("user_type")
    private Integer userType;
    private String department;
    private Integer status;
    @TableField("is_verified")
    private Integer isVerified;
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    @TableField("last_login_ip")
    private String lastLoginIp;
    @TableField("login_fail_count")
    private Integer loginFailCount;
    @TableField("lock_time")
    private LocalDateTime lockTime;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    private Integer deleted;
}