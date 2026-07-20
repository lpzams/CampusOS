package com.campus.infrastructure.persistence.score;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScoreCourseMapper extends BaseMapper<CoursePO> {

    /**
     * 按教师ID和学期查课程
     */
    @Select("<script>" +
            "SELECT * FROM t_course WHERE deleted = 0 AND teacher_id = #{teacherId} " +
            "<if test='semester != null and semester != \"\"'> AND semester = #{semester} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<CoursePO> selectByTeacherId(@Param("teacherId") Long teacherId,
                                      @Param("semester") String semester);

    /**
     * 分页查课程列表
     */
    @Select("<script>" +
            "SELECT * FROM t_course WHERE deleted = 0 " +
            "<if test='semester != null and semester != \"\"'> AND semester = #{semester} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR course_code LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<CoursePO> selectPage(@Param("semester") String semester,
                               @Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("size") int size);

    /**
     * 统计课程总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_course WHERE deleted = 0 " +
            "<if test='semester != null and semester != \"\"'> AND semester = #{semester} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR course_code LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "</script>")
    long count(@Param("semester") String semester, @Param("keyword") String keyword);

}
