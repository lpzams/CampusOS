package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user_profile")
public class UserProfilePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("student_id")
    private String studentId;
    private String major;
    @TableField("class_name")
    private String className;
    @TableField("enrollment_year")
    private String enrollmentYear;
    @TableField("teacher_id")
    private String teacherId;
    private String title;
    @TableField("research_direction")
    private String researchDirection;
    @TableField("id_card")
    private String idCard;
    @TableField("id_card_front")
    private String idCardFront;
    @TableField("id_card_back")
    private String idCardBack;
    @TableField("student_card")
    private String studentCard;
    @TableField("verify_status")
    private Integer verifyStatus;
    @TableField("verify_reason")
    private String verifyReason;
    @TableField("verified_time")
    private LocalDateTime verifiedTime;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}