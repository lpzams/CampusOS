package com.campus.infrastructure.persistence.course;

import com.campus.domain.course.entity.Schedule;
import com.campus.domain.course.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository @RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepository {
    private final ScheduleMapper mapper;
    private final CourseConverter cvt;

    @Override public Schedule findById(Long id) { return id == null ? null : cvt.toSchedule(mapper.selectById(id)); }
    @Override public Schedule findByCourseId(Long cid) { return cid == null ? null : cvt.toSchedule(mapper.selectByCourseId(cid)); }
    @Override public void save(Schedule s) { SchedulePO po = cvt.toSchedulePO(s); mapper.insert(po); s.setId(po.getId()); }
    @Override public void update(Schedule s) { mapper.updateById(cvt.toSchedulePO(s)); }
    @Override public void delete(Long id) { mapper.deleteById(id); }
    @Override public void deleteByCourseId(Long cid) { mapper.deleteByCourseId(cid); }
    @Override public List<Schedule> findPage(String sem, Long tid, int off, int sz) { return cvt.toScheduleList(mapper.selectPage(sem, tid, off, sz)); }
    @Override public long count(String sem, Long tid) { return mapper.count(sem, tid); }
    @Override public List<Schedule> findByStudentAndSemester(Long sid, String sem) { return sid == null ? Collections.emptyList() : cvt.toScheduleList(mapper.selectByStudentAndSemester(sid, sem)); }
}
