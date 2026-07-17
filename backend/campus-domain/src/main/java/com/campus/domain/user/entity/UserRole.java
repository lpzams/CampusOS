package com.campus.domain.user.entity;

import com.campus.common.constant.UserConstants;
import com.campus.common.exception.BusinessException;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Integer roleId;
    private LocalDateTime createdTime;

    // ========== 工厂方法 ==========

    /**
     * 创建用户角色关联
     */
    public static UserRole create(Long userId, Integer roleId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (roleId == null) {
            throw new BusinessException("角色ID不能为空");
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedTime(LocalDateTime.now());
        return userRole;
    }

    /**
     * 创建学生角色
     */
    public static UserRole createStudentRole(Long userId) {
        return create(userId, UserConstants.DEFAULT_ROLE_STUDENT_ID);
    }

    /**
     * 创建教师角色
     */
    public static UserRole createTeacherRole(Long userId) {
        return create(userId, UserConstants.DEFAULT_ROLE_TEACHER_ID);
    }

    /**
     * 创建管理员角色
     */
    public static UserRole createAdminRole(Long userId) {
        return create(userId, UserConstants.DEFAULT_ROLE_ADMIN_ID);
    }

    /**
     * 根据用户类型创建默认角色
     */
    public static UserRole createDefaultRole(Long userId, Integer userType) {
        if (userType == null) {
            throw new BusinessException("用户类型不能为空");
        }
        Integer roleId;
        switch (userType) {
            case UserConstants.TYPE_STUDENT:
                roleId = UserConstants.DEFAULT_ROLE_STUDENT_ID;
                break;
            case UserConstants.TYPE_TEACHER:
                roleId = UserConstants.DEFAULT_ROLE_TEACHER_ID;
                break;
            case UserConstants.TYPE_ADMIN:
                roleId = UserConstants.DEFAULT_ROLE_ADMIN_ID;
                break;
            default:
                throw new BusinessException("未知的用户类型");
        }
        return create(userId, roleId);
    }

    // ========== 业务方法 ==========

    /**
     * 判断是否为学生角色
     */
    public boolean isStudentRole() {
        return this.roleId != null && this.roleId.equals(UserConstants.DEFAULT_ROLE_STUDENT_ID);
    }

    /**
     * 判断是否为教师角色
     */
    public boolean isTeacherRole() {
        return this.roleId != null && this.roleId.equals(UserConstants.DEFAULT_ROLE_TEACHER_ID);
    }

    /**
     * 判断是否为管理员角色
     */
    public boolean isAdminRole() {
        return this.roleId != null && this.roleId.equals(UserConstants.DEFAULT_ROLE_ADMIN_ID);
    }

    /**
     * 验证角色关联是否有效
     */
    public void validate() {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (roleId == null) {
            throw new BusinessException("角色ID不能为空");
        }
        if (roleId <= 0) {
            throw new BusinessException("无效的角色ID");
        }
    }
}