package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSeatMapper extends BaseMapper<ExamSeatPO> {

    @Select("SELECT * FROM t_exam_seat WHERE deleted = 0 AND room_id = #{roomId} ORDER BY seat_number")
    List<ExamSeatPO> selectByRoomId(@Param("roomId") Long roomId);

    @Select("SELECT * FROM t_exam_seat WHERE deleted = 0 AND exam_id = #{examId}")
    List<ExamSeatPO> selectByExamId(@Param("examId") Long examId);

    @Select("SELECT * FROM t_exam_seat WHERE deleted = 0 AND student_user_id = #{studentUserId}")
    List<ExamSeatPO> selectByStudentUserId(@Param("studentUserId") Long studentUserId);
}
