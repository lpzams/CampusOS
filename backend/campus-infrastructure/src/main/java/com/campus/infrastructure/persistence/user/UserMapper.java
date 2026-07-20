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

    @Select("SELECT * FROM t_user WHERE open_id = #{openId} AND deleted = 0")
    UserPO selectByOpenId(@Param("openId") String openId);

    @Select("<script>SELECT * FROM t_user WHERE deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            " AND (username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='userType != null'> AND user_type = #{userType} </if>" +
            "ORDER BY created_time DESC LIMIT #{offset}, #{size}</script>")
    java.util.List<UserPO> selectPage(@Param("keyword") String keyword,
                                      @Param("status") Integer status,
                                      @Param("userType") Integer userType,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_user WHERE deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            " AND (username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='userType != null'> AND user_type = #{userType} </if></script>")
    long countPage(@Param("keyword") String keyword,
                   @Param("status") Integer status,
                   @Param("userType") Integer userType);
}