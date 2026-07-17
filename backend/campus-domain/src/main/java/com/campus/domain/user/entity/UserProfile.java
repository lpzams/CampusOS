package com.campus.domain.user.entity;

import com.campus.common.exception.BusinessException;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfile {

    private Long id;
    private Long userId;

    // ===== 学生字段 =====
    private String studentId;
    private String major;
    private String className;
    private String enrollmentYear;

    // ===== 教师字段 =====
    private String teacherId;
    private String title;
    private String researchDirection;

    // ===== 认证字段 =====
    private String idCard;
    private String idCardFront;
    private String idCardBack;
    private String studentCard;
    private Integer verifyStatus;
    private String verifyReason;
    private LocalDateTime verifiedTime;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // ========== 工厂方法 ==========

    /**
     * 创建学生详细信息
     */
    public static UserProfile createStudent(Long userId,
                                            String major, String className,
                                            String enrollmentYear) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setMajor(major);
        profile.setClassName(className);
        profile.setEnrollmentYear(enrollmentYear);
        profile.init();
        return profile;
    }

    /**
     * 创建教师详细信息
     */
    public static UserProfile createTeacher(Long userId,
                                            String title, String researchDirection) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setTitle(title);
        profile.setResearchDirection(researchDirection);
        profile.init();
        return profile;
    }

    /**
     * 初始化
     */
    private void init() {
        this.verifyStatus = 0;  // 未认证
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    // ========== 业务方法 ==========

    /**
     * 提交实名认证
     */
    public void submitVerify(String idCard, String idCardFront,
                             String idCardBack, String studentCard) {
        if (this.verifyStatus == 2) {
            throw new BusinessException("已认证通过，无需重复提交");
        }
        this.idCard = idCard;
        this.idCardFront = idCardFront;
        this.idCardBack = idCardBack;
        this.studentCard = studentCard;
        this.verifyStatus = 1;  // 审核中
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 认证通过
     */
    public void verifyPass() {
        if (this.verifyStatus == null || this.verifyStatus != 1) {
            throw new BusinessException("不在审核中状态，无法通过");
        }
        this.verifyStatus = 2;
        this.verifiedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 认证拒绝
     */
    public void verifyReject(String reason) {
        if (this.verifyStatus == null || this.verifyStatus != 1) {
            throw new BusinessException("不在审核中状态，无法拒绝");
        }
        this.verifyStatus = 3;
        this.verifyReason = reason;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 是否已认证
     */
    public boolean isVerified() {
        return this.verifyStatus != null && this.verifyStatus == 2;
    }

    /**
     * 是否学生
     */
    public boolean isStudent() {
        return this.studentId != null && !this.studentId.isEmpty();
    }

    /**
     * 是否教师
     */
    public boolean isTeacher() {
        return this.teacherId != null && !this.teacherId.isEmpty();
    }

    /**
     * 更新学生信息
     */
    public void updateStudentInfo(String major, String className, String enrollmentYear) {
        this.major = major;
        this.className = className;
        this.enrollmentYear = enrollmentYear;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 更新教师信息
     */
    public void updateTeacherInfo(String title, String researchDirection) {
        this.title = title;
        this.researchDirection = researchDirection;
        this.updatedTime = LocalDateTime.now();
    }
}