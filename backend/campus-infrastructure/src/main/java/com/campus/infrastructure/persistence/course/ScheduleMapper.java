package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<SchedulePO> {

    @Select("SELECT * FROM t_schedule WHERE id = #{id}")
    SchedulePO selectById(@Param("id") Long id);

    @Select("SELECT * FROM t_schedule WHERE course_id = #{courseId}")
    SchedulePO selectByCourseId(@Param("courseId") Long courseId);

    @Delete("DELETE FROM t_schedule WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);

    /** 学生课表：JOIN选课表 */
    @Select("SELECT s.*, c.name AS course_name, c.course_code, c.teacher_id, c.teacher_name, " +
            "r.name AS classroom, r.building " +
            "FROM t_schedule s " +
            "INNER JOIN t_course c ON s.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN t_classroom r ON s.classroom_id = r.id " +
            "INNER JOIN t_course_enrollment e ON c.id = e.course_id " +
            "WHERE e.student_id = #{studentId} AND e.semester = #{semester} " +
            "ORDER BY s.day_of_week, s.time_slot")
    @Results({
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "course_code", property = "courseCode"),
            @Result(column = "teacher_id", property = "teacherId"),
            @Result(column = "teacher_name", property = "teacherName"),
            @Result(column = "classroom", property = "classroom"),
            @Result(column = "building", property = "building")
    })
    List<SchedulePO> selectByStudentAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);

    /** 排课列表（管理员） */
    @Select("<script>SELECT s.*, c.name AS course_name, c.course_code, c.teacher_id, c.teacher_name, " +
            "r.name AS classroom, r.building " +
            "FROM t_schedule s " +
            "INNER JOIN t_course c ON s.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN t_classroom r ON s.classroom_id = r.id " +
            "WHERE 1=1" +
            "<if test='semester != null and semester != \"\"'> AND c.semester = #{semester}</if>" +
            "<if test='teacherId != null'> AND c.teacher_id = #{teacherId}</if>" +
            " ORDER BY s.day_of_week, s.time_slot LIMIT #{offset}, #{size}</script>")
    @Results({
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "course_code", property = "courseCode"),
            @Result(column = "teacher_id", property = "teacherId"),
            @Result(column = "teacher_name", property = "teacherName"),
            @Result(column = "classroom", property = "classroom"),
            @Result(column = "building", property = "building")
    })
    List<SchedulePO> selectPage(@Param("semester") String semester, @Param("teacherId") Long teacherId, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_schedule s INNER JOIN t_course c ON s.course_id = c.id AND c.deleted = 0 WHERE 1=1" +
            "<if test='semester != null and semester != \"\"'> AND c.semester = #{semester}</if>" +
            "<if test='teacherId != null'> AND c.teacher_id = #{teacherId}</if></script>")
    long count(@Param("semester") String semester, @Param("teacherId") Long teacherId);
}
