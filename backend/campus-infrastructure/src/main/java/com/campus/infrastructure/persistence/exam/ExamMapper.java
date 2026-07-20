package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<ExamPO> {

    /** 查所有考试（按状态可选） */
    @Select("<script>" +
            "SELECT * FROM t_exam WHERE deleted = 0 " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "ORDER BY exam_date ASC, exam_time ASC" +
            "</script>")
    List<ExamPO> selectAll(@Param("status") String status);

    /** 按月份查考试 */
    @Select("SELECT * FROM t_exam WHERE deleted = 0 " +
            "AND YEAR(exam_date) = #{year} AND MONTH(exam_date) = #{month} " +
            "ORDER BY exam_date ASC, exam_time ASC")
    List<ExamPO> selectByMonth(@Param("year") int year, @Param("month") int month);

    /** 按学生用户ID查考试（关联座位表） */
    @Select("<script>" +
            "SELECT DISTINCT e.* FROM t_exam e " +
            "INNER JOIN t_exam_seat s ON e.id = s.exam_id AND s.deleted = 0 " +
            "WHERE e.deleted = 0 AND s.student_user_id = #{studentUserId} " +
            "<if test='status != null and status != \"\"'> AND e.status = #{status} </if>" +
            "ORDER BY e.exam_date ASC, e.exam_time ASC" +
            "</script>")
    List<ExamPO> selectByStudentUserId(@Param("studentUserId") Long studentUserId,
                                         @Param("status") String status);

    /** 判断某日期某教室是否已被占用 */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM t_exam WHERE deleted = 0 " +
            "AND exam_date = #{examDate} AND classroom = #{classroom} " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByDateAndClassroom(@Param("examDate") LocalDate examDate,
                                      @Param("classroom") String classroom,
                                      @Param("excludeId") Long excludeId);
}
