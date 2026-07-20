package com.campus.domain.exam.repository;

import com.campus.domain.exam.entity.Exam;

import java.time.LocalDate;
import java.util.List;

public interface ExamRepository {

    /** 查所有考试列表（学生端，按状态可选） */
    List<Exam> findAll(String status);

    /** 按月份查考试（日历视图） */
    List<Exam> findByMonth(int year, int month);

    /** 按学生用户ID查考试（通过座位关联） */
    List<Exam> findByStudentUserId(Long studentUserId, String status);

    /** 按ID查 */
    Exam findById(Long id);

    /** 新增 */
    void save(Exam exam);

    /** 更新 */
    void update(Exam exam);

    /** 删除 */
    void delete(Long id);

    /** 判断某日期某教室是否已被占用 */
    boolean existsByDateAndClassroom(LocalDate examDate, String classroom, Long excludeId);
}
