package com.campus.domain.exam.repository;

import com.campus.domain.exam.entity.ExamSeat;

import java.util.List;

public interface ExamSeatRepository {

    /** 按考场ID查座位列表 */
    List<ExamSeat> findByRoomId(Long roomId);

    /** 按考试ID查座位列表 */
    List<ExamSeat> findByExamId(Long examId);

    /** 按学生用户ID查座位 */
    List<ExamSeat> findByStudentUserId(Long studentUserId);

    /** 批量新增座位 */
    void saveBatch(List<ExamSeat> seats);

    /** 删除某考试的所有座位 */
    void deleteByExamId(Long examId);
}
