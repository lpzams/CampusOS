package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0")
    UserPO selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deleted = 0")
    UserPO selectByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM t_user WHERE email = #{email} AND deleted = 0")
    UserPO selectByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM t_user WHERE phone = #{phone} AND deleted = 0")
    int countByPhone(@Param("phone") String phone);

    @Select("SELECT COUNT(*) FROM t_user WHERE email = #{email} AND deleted = 0")
    int countByEmail(@Param("email") String email);
}