package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_exam")
public class ExamPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("course_name")
    private String courseName;

    @TableField("course_code")
    private String courseCode;

    @TableField("exam_date")
    private LocalDate examDate;

    @TableField("exam_time")
    private String examTime;

    private String building;
    private String classroom;

    @TableField("exam_type")
    private String examType;

    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
