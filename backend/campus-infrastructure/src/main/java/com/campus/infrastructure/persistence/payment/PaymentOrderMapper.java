package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrderPO> {
    @Select("SELECT * FROM t_payment_order WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<PaymentOrderPO> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM t_payment_order WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
