package com.campus.domain.exam.repository;

import com.campus.domain.exam.entity.ExamReminder;

public interface ExamReminderRepository {

    /** 按用户ID查提醒设置 */
    ExamReminder findByUserId(Long userId);

    /** 新增或更新 */
    void saveOrUpdate(ExamReminder reminder);
}
