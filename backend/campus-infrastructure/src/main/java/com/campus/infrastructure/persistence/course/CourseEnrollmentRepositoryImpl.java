package com.campus.infrastructure.persistence.course;

import com.campus.domain.course.entity.CourseEnrollment;
import com.campus.domain.course.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository @RequiredArgsConstructor
public class CourseEnrollmentRepositoryImpl implements CourseEnrollmentRepository {
    private final CourseEnrollmentMapper mapper;
    private final CourseConverter cvt;

    @Override public void save(CourseEnrollment e) { CourseEnrollmentPO po = cvt.toEnrollmentPO(e); mapper.insert(po); e.setId(po.getId()); }
    @Override public void delete(Long courseId, Long studentId) { mapper.deleteByCourseAndStudent(courseId, studentId); }
    @Override public boolean existsByCourseAndStudent(Long cid, Long sid) { return cid != null && sid != null && mapper.exists(cid, sid) > 0; }
    @Override public long countByCourseId(Long cid) { return cid == null ? 0 : mapper.countByCourseId(cid); }
    @Override public List<CourseEnrollment> findByStudentAndSemester(Long sid, String sem) { return sid == null ? Collections.emptyList() : cvt.toEnrollmentList(mapper.selectByStudentAndSemester(sid, sem)); }
    @Override public List<Long> findStudentIdsByCourseId(Long cid) { return cid == null ? Collections.emptyList() : mapper.selectStudentIdsByCourseId(cid); }
}
