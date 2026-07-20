package com.campus.domain.score.repository;

import com.campus.domain.score.entity.Score;

import java.util.List;

public interface ScoreRepository {

    // ===== 学生端：按用户ID查询 =====

    /** 按学生用户ID和学期查询成绩列表 */
    List<Score> findByStudentUserId(Long studentUserId, String semester, String type);

    /** 统计学生成绩总条数 */
    long countByStudentUserId(Long studentUserId);

    /** 按学生用户ID查全部成绩（用于GPA计算和统计） */
    List<Score> findAllByStudentUserId(Long studentUserId);

    // ===== 教师/管理员端：按课程ID查询 =====

    /** 按课程ID和类型查询成绩列表 */
    List<Score> findByCourseId(Long courseId, String type, int offset, int size);

    /** 统计某课程的成绩条数 */
    long countByCourseId(Long courseId, String type);

    // ===== 单条操作 =====

    /** 按ID查成绩 */
    Score findById(Long id);

    /** 按课程ID、学生用户ID、类型查成绩 */
    Score findByCourseIdAndStudentUserIdAndType(Long courseId, Long studentUserId, String type);

    /** 新增成绩 */
    void save(Score score);

    /** 更新成绩 */
    void update(Score score);

    /** 批量新增成绩 */
    void saveBatch(List<Score> scores);

    /** 删除成绩 */
    void delete(Long id);
}
