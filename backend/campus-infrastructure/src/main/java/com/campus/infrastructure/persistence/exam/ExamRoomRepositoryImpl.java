package com.campus.infrastructure.persistence.exam;

import com.campus.domain.exam.entity.ExamRoom;
import com.campus.domain.exam.repository.ExamRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExamRoomRepositoryImpl implements ExamRoomRepository {

    private final ExamRoomMapper examRoomMapper;
    private final ExamConverter examConverter;

    @Override
    public List<ExamRoom> findByExamId(Long examId) {
        return examConverter.toExamRoomList(examRoomMapper.selectByExamId(examId));
    }

    @Override
    public void save(ExamRoom room) {
        ExamRoomPO po = examConverter.toExamRoomPO(room);
        examRoomMapper.insert(po);
        room.setId(po.getId());
    }

    @Override
    public void deleteByExamId(Long examId) {
        examRoomMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRoomPO>()
                .eq(ExamRoomPO::getExamId, examId));
    }
}
