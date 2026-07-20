package com.campus.infrastructure.persistence.score;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_course")
public class CoursePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("course_code")
    private String courseCode;

    private Integer credit;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("teacher_name")
    private String teacherName;

    private String semester;

    @TableField("max_students")
    private Integer maxStudents;

    private String color;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
