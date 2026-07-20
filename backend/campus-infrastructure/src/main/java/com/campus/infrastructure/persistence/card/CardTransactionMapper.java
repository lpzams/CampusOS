package com.campus.infrastructure.persistence.card;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CardTransactionMapper extends BaseMapper<CardTransactionPO> {
    @Select("<script>" +
            "SELECT * FROM t_card_transaction WHERE user_id = #{userId} " +
            "<if test='startDate != null and startDate != \"\"'> AND create_time >= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND create_time &lt;= #{endDate} </if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<CardTransactionPO> selectByUserId(@Param("userId") Long userId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_card_transaction WHERE user_id = #{userId} " +
            "<if test='startDate != null and startDate != \"\"'> AND create_time >= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND create_time &lt;= #{endDate} </if>" +
            "</script>")
    long countByUserId(@Param("userId") Long userId,
                       @Param("startDate") String startDate,
                       @Param("endDate") String endDate);
}
