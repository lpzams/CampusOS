package com.campus.domain.course.repository;

import com.campus.domain.course.entity.Classroom;

import java.util.List;

public interface ClassroomRepository {

    Classroom findById(Long id);

    void save(Classroom room);

    void update(Classroom room);

    void delete(Long id);

    List<Classroom> findPage(String building, int offset, int size);

    long count(String building);

    List<Classroom> findAll(String building);

    /** 查某时段被占用的教室ID集合 */
    List<Long> findOccupiedIds(int dayOfWeek, String timeSlot);

    /** 按教学楼+教室名查教室 */
    Classroom findByBuildingAndName(String building, String name);
}
