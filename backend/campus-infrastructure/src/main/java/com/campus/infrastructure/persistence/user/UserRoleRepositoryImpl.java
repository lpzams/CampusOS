package com.campus.infrastructure.persistence.user;

import com.campus.domain.user.entity.UserRole;
import com.campus.domain.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色关联仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleMapper userRoleMapper;
    private final UserConverter userConverter;

    @Override
    public void save(UserRole userRole) {
        if (userRole == null) {
            return;
        }
        if (userRole.getCreatedTime() == null) {
            userRole.setCreatedTime(LocalDateTime.now());
        }
        UserRolePO po = userConverter.toUserRolePO(userRole);
        if (po.getId() == null) {
            userRoleMapper.insert(po);
            userRole.setId(po.getId());
        } else {
            userRoleMapper.updateById(po);
        }
    }

    @Override
    public void saveBatch(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return;
        }
        for (UserRole userRole : userRoles) {
            if (userRole.getCreatedTime() == null) {
                userRole.setCreatedTime(LocalDateTime.now());
            }
        }
        List<UserRolePO> poList = userConverter.toUserRolePOList(userRoles);
        for (UserRolePO po : poList) {
            userRoleMapper.insert(po);
        }
    }

    @Override
    public List<UserRole> findByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<UserRolePO> poList = userRoleMapper.selectByUserId(userId);
        return userConverter.toUserRoleList(poList);
    }

    @Override
    public void deleteByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        userRoleMapper.deleteByUserId(userId);
    }

    @Override
    public void deleteByUserIdAndRoleId(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return;
        }
        userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public boolean existsByUserIdAndRoleId(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return false;
        }
        return userRoleMapper.countByUserIdAndRoleId(userId, roleId) > 0;
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return userRoleMapper.selectUserIdsByRoleId(roleId);
    }
}