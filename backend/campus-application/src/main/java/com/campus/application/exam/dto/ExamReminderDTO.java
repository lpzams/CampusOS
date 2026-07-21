package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 考试提醒设置响应（7.10）
 */
@Data
@Builder
public class ExamReminderDTO {

    private Boolean enabled;
    private Integer reminderMinutes;
}
