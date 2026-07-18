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
    private String dormitory;
    private String advisor;

    // ===== 教师字段 =====
    private String teacherId;
    private String title;
    private String researchDirection;
    private String office;
    private String introduction;

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

    public static UserProfile createStudent(Long userId, String studentId,
                                            String major, String className,
                                            String enrollmentYear) {
        UserProfile profile = new UserProfile();
        profile.setStudentId(studentId);
        profile.setUserId(userId);
        profile.setMajor(major);
        profile.setClassName(className);
        profile.setEnrollmentYear(enrollmentYear);
        profile.init();
        return profile;
    }

    public static UserProfile createTeacher(Long userId,
                                            String title, String researchDirection) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setTitle(title);
        profile.setResearchDirection(researchDirection);
        profile.init();
        return profile;
    }

    private void init() {
        this.verifyStatus = 0;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    // ========== 业务方法 ==========

    /**
     * 提交实名认证
     */
    public void submitVerify(String realName, String idCard,
                             String idCardFront, String idCardBack,
                             String studentId) {
        if (this.verifyStatus != null && this.verifyStatus == 2) {
            throw new BusinessException("已认证通过，无需重复提交");
        }
        if (this.verifyStatus != null && this.verifyStatus == 1) {
            throw new BusinessException("实名认证审核中，请耐心等待");
        }
        this.idCard = idCard;
        this.idCardFront = idCardFront;
        this.idCardBack = idCardBack;
        this.studentId = studentId;
        this.verifyStatus = 1;  // 审核中
        this.verifyReason = null;
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
     * 更新基本信息（仅允许修改：realName 在 User 层、phone、email、gender）
     * Profile 层更新学生/教师特有字段
     */
    public void updateStudentInfo(String major, String className, String enrollmentYear,
                                  String dormitory, String advisor) {
        this.major = major;
        this.className = className;
        this.enrollmentYear = enrollmentYear;
        this.dormitory = dormitory;
        this.advisor = advisor;
        this.updatedTime = LocalDateTime.now();
    }

    public void updateTeacherInfo(String title, String researchDirection,
                                  String office, String introduction) {
        this.title = title;
        this.researchDirection = researchDirection;
        this.office = office;
        this.introduction = introduction;
        this.updatedTime = LocalDateTime.now();
    }

    public boolean isVerified() {
        return this.verifyStatus != null && this.verifyStatus == 2;
    }

    public boolean isStudent() {
        return this.studentId != null && !this.studentId.isEmpty();
    }

    public boolean isTeacher() {
        return this.teacherId != null && !this.teacherId.isEmpty();
    }
}
