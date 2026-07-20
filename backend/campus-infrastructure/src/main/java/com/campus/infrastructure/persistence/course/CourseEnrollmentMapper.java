package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollmentPO> {

    @Select("SELECT COUNT(*) FROM t_course_enrollment WHERE course_id = #{courseId} AND student_id = #{studentId}")
    int exists(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("SELECT COUNT(*) FROM t_course_enrollment WHERE course_id = #{courseId}")
    long countByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM t_course_enrollment WHERE student_id = #{studentId} AND semester = #{semester} ORDER BY create_time DESC")
    List<CourseEnrollmentPO> selectByStudentAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);

    @Select("SELECT student_id FROM t_course_enrollment WHERE course_id = #{courseId}")
    List<Long> selectStudentIdsByCourseId(@Param("courseId") Long courseId);

    @Delete("DELETE FROM t_course_enrollment WHERE course_id = #{courseId} AND student_id = #{studentId}")
    int deleteByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}
