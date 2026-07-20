package com.campus.infrastructure.persistence.score;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<ScorePO> {

    /**
     * 按学生用户ID和学期查询成绩（关联课程表获取学期信息）
     */
    @Select("<script>" +
            "SELECT s.* FROM t_score s " +
            "INNER JOIN t_course c ON s.course_id = c.id AND c.deleted = 0 " +
            "WHERE s.deleted = 0 AND s.student_user_id = #{studentUserId} " +
            "<if test='semester != null and semester != \"\"'> AND c.semester = #{semester} </if>" +
            "<if test='type != null and type != \"\"'> AND s.type = #{type} </if>" +
            "ORDER BY s.create_time DESC" +
            "</script>")
    List<ScorePO> selectByStudentUserId(@Param("studentUserId") Long studentUserId,
                                        @Param("semester") String semester,
                                        @Param("type") String type);

    /**
     * 按学生用户ID查全部成绩
     */
    @Select("SELECT * FROM t_score WHERE deleted = 0 AND student_user_id = #{studentUserId}")
    List<ScorePO> selectAllByStudentUserId(@Param("studentUserId") Long studentUserId);

    /**
     * 按课程ID和类型分页查成绩
     */
    @Select("<script>" +
            "SELECT * FROM t_score WHERE deleted = 0 AND course_id = #{courseId} " +
            "<if test='type != null and type != \"\"'> AND type = #{type} </if>" +
            "ORDER BY score DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<ScorePO> selectByCourseId(@Param("courseId") Long courseId,
                                    @Param("type") String type,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    /**
     * 统计某课程成绩条数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_score WHERE deleted = 0 AND course_id = #{courseId} " +
            "<if test='type != null and type != \"\"'> AND type = #{type} </if>" +
            "</script>")
    long countByCourseId(@Param("courseId") Long courseId, @Param("type") String type);

    /**
     * 按课程ID、学生用户ID、类型查唯一成绩
     */
    @Select("SELECT * FROM t_score WHERE deleted = 0 AND course_id = #{courseId} " +
            "AND student_user_id = #{studentUserId} AND type = #{type}")
    ScorePO selectByCourseIdAndStudentUserIdAndType(@Param("courseId") Long courseId,
                                                      @Param("studentUserId") Long studentUserId,
                                                      @Param("type") String type);

    /**
     * 统计所有成绩条数（用于排名计算）
     */
    @Select("SELECT COUNT(DISTINCT student_user_id) FROM t_score WHERE deleted = 0")
    long countDistinctStudents();
}
