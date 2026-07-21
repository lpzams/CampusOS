package com.campus.infrastructure.persistence.score;

import com.campus.domain.score.entity.Score;
import com.campus.domain.score.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ScoreRepositoryImpl implements ScoreRepository {

    private final ScoreMapper scoreMapper;
    private final ScoreConverter scoreConverter;

    @Override
    public List<Score> findByStudentUserId(Long studentUserId, String semester, String type) {
        return scoreConverter.toScoreList(
                scoreMapper.selectByStudentUserId(studentUserId, semester, type));
    }

    @Override
    public long countByStudentUserId(Long studentUserId) {
        // 不常用，通过 Mapper 的 selectCount 包装查询
        return scoreMapper.selectAllByStudentUserId(studentUserId).size();
    }

    @Override
    public List<Score> findAllByStudentUserId(Long studentUserId) {
        return scoreConverter.toScoreList(
                scoreMapper.selectAllByStudentUserId(studentUserId));
    }

    @Override
    public List<Score> findByCourseId(Long courseId, String type, int offset, int size) {
        return scoreConverter.toScoreList(
                scoreMapper.selectByCourseId(courseId, type, offset, size));
    }

    @Override
    public long countByCourseId(Long courseId, String type) {
        return scoreMapper.countByCourseId(courseId, type);
    }

    @Override
    public Score findById(Long id) {
        if (id == null) return null;
        return scoreConverter.toScore(scoreMapper.selectById(id));
    }

    @Override
    public Score findByCourseIdAndStudentUserIdAndType(Long courseId, Long studentUserId, String type) {
        return scoreConverter.toScore(
                scoreMapper.selectByCourseIdAndStudentUserIdAndType(courseId, studentUserId, type));
    }

    @Override
    public void save(Score score) {
        ScorePO po = scoreConverter.toScorePO(score);
        scoreMapper.insert(po);
        score.setId(po.getId());
    }

    @Override
    public void update(Score score) {
        ScorePO po = scoreConverter.toScorePO(score);
        scoreMapper.updateById(po);
    }

    @Override
    public void saveBatch(List<Score> scores) {
        if (scores == null || scores.isEmpty()) return;
        List<ScorePO> poList = new ArrayList<>();
        for (Score score : scores) {
            poList.add(scoreConverter.toScorePO(score));
        }
        // 逐条插入以保证数据一致性
        for (ScorePO po : poList) {
            scoreMapper.insert(po);
        }
    }

    @Override
    public void delete(Long id) {
        scoreMapper.deleteById(id);
    }
}
