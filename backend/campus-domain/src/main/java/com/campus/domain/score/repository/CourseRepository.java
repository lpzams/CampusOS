package com.campus.domain.score.repository;

import com.campus.domain.score.entity.Course;

import java.util.List;

public interface CourseRepository {

    /** 按ID查课程 */
    Course findById(Long id);

    /** 按教师ID和学期查课程 */
    List<Course> findByTeacherId(Long teacherId, String semester);

    /** 分页查课程列表 */
    List<Course> findPage(String semester, String keyword, int offset, int size);

    /** 统计课程总数 */
    long count(String semester, String keyword);

    /** 新增课程 */
    void save(Course course);

    /** 更新课程 */
    void update(Course course);

    /** 删除课程（逻辑删除） */
    void delete(Long id);

    /** 判断课程是否存在 */
    boolean existsById(Long id);
}
