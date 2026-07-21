package com.campus.domain.exam.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamReminder {

    private Long id;
    private Long userId;
    private Boolean enabled;
    private Integer reminderMinutes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 工厂方法 ==========

    public static ExamReminder create(Long userId) {
        ExamReminder reminder = new ExamReminder();
        reminder.setUserId(userId);
        reminder.setEnabled(true);
        reminder.setReminderMinutes(60);
        reminder.setCreateTime(LocalDateTime.now());
        reminder.setUpdateTime(LocalDateTime.now());
        return reminder;
    }

    // ========== 业务方法 ==========

    public void updateSettings(Boolean enabled, Integer reminderMinutes) {
        if (enabled != null) this.enabled = enabled;
        if (reminderMinutes != null) this.reminderMinutes = reminderMinutes;
        this.updateTime = LocalDateTime.now();
    }
}
