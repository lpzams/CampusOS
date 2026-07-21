package com.campus.infrastructure.persistence.score;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_score")
public class ScorePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("student_user_id")
    private Long studentUserId;

    private Integer score;
    private String grade;
    private String type;

    @TableField("exam_time")
    private LocalDate examTime;

    private String reason;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
