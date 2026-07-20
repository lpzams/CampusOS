package com.campus.infrastructure.persistence.exam;

import com.campus.domain.exam.entity.ExamReminder;
import com.campus.domain.exam.repository.ExamReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExamReminderRepositoryImpl implements ExamReminderRepository {

    private final ExamReminderMapper examReminderMapper;
    private final ExamConverter examConverter;

    @Override
    public ExamReminder findByUserId(Long userId) {
        return examConverter.toExamReminder(examReminderMapper.selectByUserId(userId));
    }

    @Override
    public void saveOrUpdate(ExamReminder reminder) {
        ExamReminderPO po = examConverter.toExamReminderPO(reminder);
        ExamReminderPO existing = examReminderMapper.selectByUserId(reminder.getUserId());
        if (existing != null) {
            po.setId(existing.getId());
            examReminderMapper.updateById(po);
        } else {
            examReminderMapper.insert(po);
            reminder.setId(po.getId());
        }
    }
}
