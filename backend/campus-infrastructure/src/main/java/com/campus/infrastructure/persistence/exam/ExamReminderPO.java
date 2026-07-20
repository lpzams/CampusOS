package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_exam_reminder")
public class ExamReminderPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private Integer enabled;

    @TableField("reminder_minutes")
    private Integer reminderMinutes;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
