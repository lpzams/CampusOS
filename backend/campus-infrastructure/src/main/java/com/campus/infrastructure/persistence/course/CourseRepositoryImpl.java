package com.campus.infrastructure.persistence.course;

import com.campus.domain.course.entity.Course;
import com.campus.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository @RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {
    private final CourseMapper mapper;
    private final CourseConverter cvt;

    @Override public Course findById(Long id) { return id == null ? null : cvt.toCourse(mapper.selectById(id)); }
    @Override public void save(Course c) { CoursePO po = cvt.toCoursePO(c); mapper.insert(po); c.setId(po.getId()); }
    @Override public void update(Course c) { mapper.updateById(cvt.toCoursePO(c)); }
    @Override public void delete(Long id) { mapper.deleteById(id); }
    @Override public List<Course> findPage(String s, String kw, int off, int sz) { return cvt.toCourseList(mapper.selectPage(s, kw, off, sz)); }
    @Override public long count(String s, String kw) { return mapper.count(s, kw); }
    @Override public List<Course> findByTeacherAndSemester(Long tid, String sem) { return tid == null ? Collections.emptyList() : cvt.toCourseList(mapper.selectByTeacherAndSemester(tid, sem)); }
    @Override public List<Course> findAvailable(String s, String kw, int off, int sz) { return cvt.toCourseList(mapper.selectAvailable(s, kw, off, sz)); }
    @Override public long countAvailable(String s, String kw) { return mapper.countAvailable(s, kw); }
    @Override public boolean existsByCourseCode(String code) { return code != null && mapper.countByCourseCode(code) > 0; }
    @Override public boolean existsByCourseCodeExcludingId(String code, Long id) { return code != null && mapper.countByCourseCodeExcludingId(code, id) > 0; }
}
