package com.campus.infrastructure.persistence.score;

import com.campus.domain.score.entity.Course;
import com.campus.domain.score.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {

    private final CourseMapper courseMapper;
    private final ScoreConverter scoreConverter;

    @Override
    public Course findById(Long id) {
        if (id == null) return null;
        return scoreConverter.toCourse(courseMapper.selectById(id));
    }

    @Override
    public List<Course> findByTeacherId(Long teacherId, String semester) {
        return scoreConverter.toCourseList(
                courseMapper.selectByTeacherId(teacherId, semester));
    }

    @Override
    public List<Course> findPage(String semester, String keyword, int offset, int size) {
        return scoreConverter.toCourseList(
                courseMapper.selectPage(semester, keyword, offset, size));
    }

    @Override
    public long count(String semester, String keyword) {
        return courseMapper.count(semester, keyword);
    }

    @Override
    public void save(Course course) {
        CoursePO po = scoreConverter.toCoursePO(course);
        courseMapper.insert(po);
        course.setId(po.getId());
    }

    @Override
    public void update(Course course) {
        CoursePO po = scoreConverter.toCoursePO(course);
        courseMapper.updateById(po);
    }

    @Override
    public void delete(Long id) {
        courseMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        return courseMapper.selectById(id) != null;
    }
}
