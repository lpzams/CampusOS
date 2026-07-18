package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户角色关联表 t_user_role 的映射
 */
@Data
@TableName("t_user_role")
public class UserRolePO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("role_id")
    private Integer roleId;

    @TableField("created_time")
    private LocalDateTime createdTime;
}