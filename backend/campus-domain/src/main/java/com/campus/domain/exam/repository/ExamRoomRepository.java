package com.campus.domain.exam.repository;

import com.campus.domain.exam.entity.ExamRoom;

import java.util.List;

public interface ExamRoomRepository {

    /** 按考试ID查所有考场 */
    List<ExamRoom> findByExamId(Long examId);

    /** 新增考场 */
    void save(ExamRoom room);

    /** 删除某考试的所有考场 */
    void deleteByExamId(Long examId);
}
