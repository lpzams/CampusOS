package com.campus.domain.user.entity;

import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private Integer userType;
    private String department;
    private Integer status;
    private Integer isVerified;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginFailCount;
    private LocalDateTime lockTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * 注册校验
     */
    public void validateForRegister() {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "用户名不能为空");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "密码不能为空");
        }
        if (realName == null || realName.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "真实姓名不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "手机号不能为空");
        }
        if (userType == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "用户类型不能为空");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "院系/部门不能为空");
        }
    }

    /**
     * 注册创建用户
     * Domain 层自己管理创建逻辑，Application 层不需要知道内部细节
     */
    public static User createForRegister(String username, String encodedPassword,
                                         String realName, String phone, String email,
                                         Integer userType, String department) {
        // 1. 创建 User 对象
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setUserType(userType);
        user.setDepartment(department);

        // 2. 调用自身的业务规则（不是外部 set）
        user.activateForRegister();

        return user;
    }

    /**
     * 管理员创建用户（不同场景）
     */
    public static User createByAdmin(String username, String encodedPassword,
                                     String realName, String phone, Integer userType,
                                     String department, Integer status) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setUserType(userType);
        user.setDepartment(department);
        user.setStatus(status);
        user.setIsVerified(0);
        user.setLoginFailCount(0);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        return user;
    }

    // ========== 业务规则 ==========

    /**
     * 注册激活
     * 规则：注册后状态为"待审核"
     */
    public void activateForRegister() {
        this.status = 2;
        this.isVerified = 0;
        this.loginFailCount = 0;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 审核通过
     */
    public void approve() {
        if (this.status == null || this.status != 2) {
            throw new BusinessException("用户不在待审核状态");
        }
        this.status = 0;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 冻结
     */
    public void freeze() {
        if (this.status == null) {
            throw new BusinessException("用户状态异常");
        }
        if (this.status == 3) {
            throw new BusinessException("已注销用户无法冻结");
        }
        this.status = 1;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 解冻
     */
    public void unfreeze() {
        if (this.status == null || this.status != 1) {
            throw new BusinessException("账号未冻结，无需解冻");
        }
        this.status = 0;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 登录成功
     */
    public void loginSuccess(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.loginFailCount = 0;
        this.lockTime = null;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 登录失败
     */
    public void loginFailed() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.lockTime = LocalDateTime.now();
        }
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 检查锁定状态
     */
    public void checkLock() {
        if (this.lockTime != null) {
            LocalDateTime unlockTime = this.lockTime.plusMinutes(30);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                long minutes = java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
                throw new BusinessException("账号已被锁定，请 " + minutes + " 分钟后重试");
            }
            this.loginFailCount = 0;
            this.lockTime = null;
        }
    }

    /**
     * 检查用户状态
     */
    public void checkStatus() {
        if (this.status == null) {
            throw new BusinessException("用户状态异常");
        }
        if (this.status == 1) {
            throw new BusinessException("账号已被冻结，请联系管理员");
        }
        if (this.status == 2) {
            throw new BusinessException("账号待审核，请等待管理员审核");
        }
        if (this.status == 3) {
            throw new BusinessException("账号已注销");
        }
    }

    /**
     * 判断身份
     */
    public boolean isStudent() {
        return this.userType != null && this.userType == 1;
    }

    public boolean isTeacher() {
        return this.userType != null && this.userType == 2;
    }

    public boolean isAdmin() {
        return this.userType != null && this.userType == 3;
    }
}