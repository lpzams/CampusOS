package com.campus.infrastructure.persistence.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {

    /**
     * 根据用户ID查询角色关联
     */
    @Select("SELECT * FROM t_user_role WHERE user_id = #{userId}")
    List<UserRolePO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除所有角色关联
     */
    @Delete("DELETE FROM t_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和角色ID删除关联
     */
    @Delete("DELETE FROM t_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 检查用户是否拥有某个角色
     */
    @Select("SELECT COUNT(*) FROM t_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据角色ID查询用户ID列表
     */
    @Select("SELECT user_id FROM t_user_role WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}