package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<PaymentPO> {
    @Select("SELECT * FROM t_payment WHERE deleted = 0 AND user_id = #{userId} ORDER BY create_time DESC")
    List<PaymentPO> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_payment WHERE deleted = 0 AND user_id = #{userId} AND status = 'PENDING'")
    List<PaymentPO> selectPendingByUserId(@Param("userId") Long userId);
}
