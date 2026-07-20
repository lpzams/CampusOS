package com.campus.infrastructure.persistence.exam;

import com.campus.domain.exam.entity.ExamSeat;
import com.campus.domain.exam.repository.ExamSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExamSeatRepositoryImpl implements ExamSeatRepository {

    private final ExamSeatMapper examSeatMapper;
    private final ExamConverter examConverter;

    @Override
    public List<ExamSeat> findByRoomId(Long roomId) {
        return examConverter.toExamSeatList(examSeatMapper.selectByRoomId(roomId));
    }

    @Override
    public List<ExamSeat> findByExamId(Long examId) {
        return examConverter.toExamSeatList(examSeatMapper.selectByExamId(examId));
    }

    @Override
    public List<ExamSeat> findByStudentUserId(Long studentUserId) {
        return examConverter.toExamSeatList(examSeatMapper.selectByStudentUserId(studentUserId));
    }

    @Override
    public void saveBatch(List<ExamSeat> seats) {
        if (seats == null || seats.isEmpty()) return;
        List<ExamSeatPO> poList = new ArrayList<>();
        for (ExamSeat seat : seats) {
            poList.add(examConverter.toExamSeatPO(seat));
        }
        for (ExamSeatPO po : poList) {
            examSeatMapper.insert(po);
        }
    }

    @Override
    public void deleteByExamId(Long examId) {
        examSeatMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamSeatPO>()
                .eq(ExamSeatPO::getExamId, examId));
    }
}
