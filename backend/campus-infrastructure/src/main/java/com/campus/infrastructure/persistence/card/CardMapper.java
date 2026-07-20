package com.campus.infrastructure.persistence.card;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CardMapper extends BaseMapper<CardPO> {
    @Select("SELECT * FROM t_card WHERE user_id = #{userId}")
    CardPO selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_card WHERE card_id = #{cardId}")
    CardPO selectByCardId(@Param("cardId") String cardId);
}
