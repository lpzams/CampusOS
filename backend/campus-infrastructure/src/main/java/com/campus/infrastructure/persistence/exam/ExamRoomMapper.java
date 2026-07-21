package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamRoomMapper extends BaseMapper<ExamRoomPO> {

    @Select("SELECT * FROM t_exam_room WHERE deleted = 0 AND exam_id = #{examId}")
    List<ExamRoomPO> selectByExamId(@Param("examId") Long examId);
}
