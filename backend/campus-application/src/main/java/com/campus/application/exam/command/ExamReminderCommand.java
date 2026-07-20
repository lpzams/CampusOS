package com.campus.application.exam.command;

import lombok.Data;

/**
 * 考试提醒设置命令（7.10）
 */
@Data
public class ExamReminderCommand {

    private Boolean enabled;
    private Integer reminderMinutes;
}
