package com.campus.domain.user.repository;

import com.campus.domain.user.entity.UserRole;
import java.util.List;

/**
 * 用户角色仓储接口
 */
public interface UserRoleRepository {

    /**
     * 保存用户角色关联
     */
    void save(UserRole userRole);

    /**
     * 批量保存用户角色关联
     */
    void saveBatch(List<UserRole> userRoles);

    /**
     * 根据用户ID查询角色列表
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据用户ID删除所有角色关联
     */
    void deleteByUserId(Long userId);

    /**
     * 根据用户ID和角色ID删除关联
     */
    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 检查用户是否拥有某个角色
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据角色ID查询用户ID列表
     */
    List<Long> findUserIdsByRoleId(Long roleId);
}