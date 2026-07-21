package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExamReminderMapper extends BaseMapper<ExamReminderPO> {

    @Select("SELECT * FROM t_exam_reminder WHERE user_id = #{userId}")
    ExamReminderPO selectByUserId(@Param("userId") Long userId);
}
