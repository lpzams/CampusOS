package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<CoursePO> {

    @Select("SELECT * FROM t_course WHERE id = #{id} AND deleted = 0")
    CoursePO selectById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM t_course WHERE course_code = #{code} AND deleted = 0")
    int countByCourseCode(@Param("code") String code);

    @Select("SELECT COUNT(*) FROM t_course WHERE course_code = #{code} AND id != #{excludeId} AND deleted = 0")
    int countByCourseCodeExcludingId(@Param("code") String code, @Param("excludeId") Long excludeId);

    @Select("<script>SELECT * FROM t_course WHERE teacher_id = #{teacherId} AND semester = #{semester} AND deleted = 0 ORDER BY name</script>")
    List<CoursePO> selectByTeacherAndSemester(@Param("teacherId") Long teacherId, @Param("semester") String semester);

    @Select("<script>SELECT * FROM t_course WHERE deleted = 0" +
            "<if test='semester != null and semester != \"\"'> AND semester = #{semester}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR course_code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY create_time DESC LIMIT #{offset}, #{size}</script>")
    List<CoursePO> selectPage(@Param("semester") String semester, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_course WHERE deleted = 0" +
            "<if test='semester != null and semester != \"\"'> AND semester = #{semester}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR course_code LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("semester") String semester, @Param("keyword") String keyword);

    /** 可选课程（有排课记录的课程） */
    @Select("<script>SELECT c.* FROM t_course c INNER JOIN t_schedule s ON c.id = s.course_id WHERE c.deleted = 0" +
            "<if test='semester != null and semester != \"\"'> AND c.semester = #{semester}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (c.name LIKE CONCAT('%',#{keyword},'%') OR c.course_code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY c.create_time DESC LIMIT #{offset}, #{size}</script>")
    List<CoursePO> selectAvailable(@Param("semester") String semester, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_course c INNER JOIN t_schedule s ON c.id = s.course_id WHERE c.deleted = 0" +
            "<if test='semester != null and semester != \"\"'> AND c.semester = #{semester}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (c.name LIKE CONCAT('%',#{keyword},'%') OR c.course_code LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long countAvailable(@Param("semester") String semester, @Param("keyword") String keyword);
}
