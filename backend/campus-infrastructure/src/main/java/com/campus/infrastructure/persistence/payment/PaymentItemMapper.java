package com.campus.infrastructure.persistence.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PaymentItemMapper extends BaseMapper<PaymentItemPO> {
    @Select("SELECT * FROM t_payment_item WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<PaymentItemPO> selectPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM t_payment_item WHERE deleted = 0")
    long count();

    @Select("SELECT COUNT(*) FROM t_payment WHERE deleted = 0 AND item_id = #{itemId} AND status = 'PAID'")
    int countPaidByItemId(@Param("itemId") Long itemId);

    @Select("SELECT COUNT(*) FROM t_payment WHERE deleted = 0 AND item_id = #{itemId}")
    int countTotalByItemId(@Param("itemId") Long itemId);
}
