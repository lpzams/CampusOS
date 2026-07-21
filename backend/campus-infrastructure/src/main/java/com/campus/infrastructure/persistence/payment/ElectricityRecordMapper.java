package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ElectricityRecordMapper extends BaseMapper<ElectricityRecordPO> {
    @Select("SELECT * FROM t_electricity_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ElectricityRecordPO> selectByUserId(@Param("userId") Long userId);
}
