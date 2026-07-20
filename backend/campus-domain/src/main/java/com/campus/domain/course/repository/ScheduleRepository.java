package com.campus.domain.course.repository;

import com.campus.domain.course.entity.Schedule;

import java.util.List;

public interface ScheduleRepository {

    Schedule findById(Long id);

    Schedule findByCourseId(Long courseId);

    void save(Schedule schedule);

    void update(Schedule schedule);

    void delete(Long id);

    void deleteByCourseId(Long courseId);

    List<Schedule> findPage(String semester, Long teacherId, int offset, int size);

    long count(String semester, Long teacherId);

    /** 查学生某学期所有排课（通过选课表关联） */
    List<Schedule> findByStudentAndSemester(Long studentId, String semester);
}
