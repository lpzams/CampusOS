package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfilePO> {

    @Select("SELECT * FROM t_user_profile WHERE user_id = #{userId}")
    UserProfilePO selectByUserId(@Param("userId") Long userId);
}