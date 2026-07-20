package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;

@Mapper
public interface ElectricityMapper extends BaseMapper<ElectricityBalancePO> {
    @Select("SELECT balance FROM t_electricity_balance WHERE dormitory_id = #{dormitoryId}")
    BigDecimal selectBalance(@Param("dormitoryId") String dormitoryId);

    @Update("UPDATE t_electricity_balance SET balance = #{newBalance} WHERE dormitory_id = #{dormitoryId}")
    int updateBalance(@Param("dormitoryId") String dormitoryId, @Param("newBalance") BigDecimal newBalance);
}
