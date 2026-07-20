package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfilePO> {

    @Select("SELECT * FROM t_user_profile WHERE user_id = #{userId}")
    UserProfilePO selectByUserId(@Param("userId") Long userId);

    @Select("<script>SELECT * FROM t_user_profile WHERE user_id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    java.util.List<UserProfilePO> selectByUserIds(@Param("userIds") java.util.List<Long> userIds);
}