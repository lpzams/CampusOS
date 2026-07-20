package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("t_schedule")
public class SchedulePO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("course_id") private Long courseId;
    @TableField("classroom_id") private Long classroomId;
    @TableField("day_of_week") private Integer dayOfWeek;
    @TableField("time_slot") private String timeSlot;
    private String weeks;
    @TableField("create_time") private LocalDateTime createTime;
    // ===== 联表查询字段（非数据库字段） =====

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String courseCode;

    @TableField(exist = false)
    private Long teacherId;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String classroom;

    @TableField(exist = false)
    private String building;
}
