package com.campus.infrastructure.persistence.course;

import com.campus.domain.course.entity.Classroom;
import com.campus.domain.course.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository @RequiredArgsConstructor
public class ClassroomRepositoryImpl implements ClassroomRepository {
    private final ClassroomMapper mapper;
    private final CourseConverter cvt;

    @Override public Classroom findById(Long id) { return id == null ? null : cvt.toClassroom(mapper.selectById(id)); }
    @Override public void save(Classroom r) { ClassroomPO po = cvt.toClassroomPO(r); mapper.insert(po); r.setId(po.getId()); }
    @Override public void update(Classroom r) { mapper.updateById(cvt.toClassroomPO(r)); }
    @Override public void delete(Long id) { mapper.deleteById(id); }
    @Override public List<Classroom> findPage(String b, int off, int sz) { return cvt.toClassroomList(mapper.selectPage(b, off, sz)); }
    @Override public long count(String b) { return mapper.count(b); }
    @Override public List<Classroom> findAll(String b) { return cvt.toClassroomList(mapper.selectAll(b)); }
    @Override public List<Long> findOccupiedIds(int dow, String ts) { return mapper.selectOccupiedIds(dow, ts); }

    /** 按教学楼+教室名查找，用于CourseAdminService自动创建教室 */
    public Classroom findByBuildingAndName(String building, String name) {
        if (building == null || name == null) return null;
        return cvt.toClassroom(mapper.selectByBuildingAndName(building, name));
    }
}
