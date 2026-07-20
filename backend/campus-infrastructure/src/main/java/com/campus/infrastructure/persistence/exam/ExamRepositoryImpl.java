package com.campus.infrastructure.persistence.exam;

import com.campus.domain.exam.entity.Exam;
import com.campus.domain.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExamRepositoryImpl implements ExamRepository {

    private final ExamMapper examMapper;
    private final ExamConverter examConverter;

    @Override
    public List<Exam> findAll(String status) {
        return examConverter.toExamList(examMapper.selectAll(status));
    }

    @Override
    public List<Exam> findByMonth(int year, int month) {
        return examConverter.toExamList(examMapper.selectByMonth(year, month));
    }

    @Override
    public List<Exam> findByStudentUserId(Long studentUserId, String status) {
        return examConverter.toExamList(examMapper.selectByStudentUserId(studentUserId, status));
    }

    @Override
    public Exam findById(Long id) {
        if (id == null) return null;
        return examConverter.toExam(examMapper.selectById(id));
    }

    @Override
    public void save(Exam exam) {
        ExamPO po = examConverter.toExamPO(exam);
        examMapper.insert(po);
        exam.setId(po.getId());
    }

    @Override
    public void update(Exam exam) {
        examMapper.updateById(examConverter.toExamPO(exam));
    }

    @Override
    public void delete(Long id) {
        examMapper.deleteById(id);
    }

    @Override
    public boolean existsByDateAndClassroom(LocalDate examDate, String classroom, Long excludeId) {
        return examMapper.existsByDateAndClassroom(examDate, classroom, excludeId);
    }
}
