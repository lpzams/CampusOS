package com.campus.infrastructure.persistence.user;

import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;
import com.campus.domain.user.repository.UserRepository;
import com.campus.infrastructure.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户仓储实现 —— Cache-Aside 模式
 * <p>
 * 读：缓存命中 → 直接返回；缓存未命中 → 查 DB → 回填缓存 → 返回
 * 写：先更新 DB → 再删除缓存（下次读自动回填最新数据）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserConverter userConverter;
    private final UserCacheService userCacheService;

    // ==================== 写操作（DB 优先 → 删缓存） ====================

    @Override
    public void save(User user) {
        UserPO po = userConverter.toUserPO(user);
        if (po.getId() == null) {
            // 新增：直接插入
            userMapper.insert(po);
            user.setId(po.getId());
        } else {
            // 更新：先写 DB → 再删缓存
            userMapper.updateById(po);
            userCacheService.evictUser(user);
        }
    }

    @Override
    @Transactional
    public void save(User user, UserProfile profile, UserRole role) {
        log.info("新增用户: {}", user);
        // 1. 插入 user 表
        UserPO userPO = userConverter.toUserPO(user);
        userMapper.insert(userPO);
        Long userId = userPO.getId();
        user.setId(userId);

        // 2. 插入 profile
        if (profile != null) {
            profile.setUserId(userId);
            UserProfilePO profilePO = userConverter.toUserProfilePO(profile);
            userProfileMapper.insert(profilePO);
        }
        log.info("保存用户信息：{}", profile);

        // 3. 插入角色
        role = UserRole.createDefaultRole(userId, user.getUserType());
        log.info("保存用户角色：{}", role);
        if (role != null) {
            role.setUserId(userId);
            UserRolePO rolePO = userConverter.toUserRolePO(role);
            userRoleMapper.insert(rolePO);
        }
        // 新增用户无旧缓存，无需 evict
    }

    @Override
    public void update(User user) {
        // 1. 先写 DB
        UserPO po = userConverter.toUserPO(user);
        userMapper.updateById(po);
        // 2. 再删缓存
        userCacheService.evictUser(user);
    }

    // ==================== 读操作（缓存优先 → DB 兜底 → 回填） ====================

    @Override
    public User findByUsername(String username) {
        if (username == null) return null;

        // 1. 二级映射缓存：username → userId
        Long userId = userCacheService.getUserIdByUsername(username);
        if (userId != null) {
            // 2. 一级缓存：user:info:{userId}
            User cached = userCacheService.getUser(userId);
            if (cached != null) return cached;
        }

        // 3. 缓存未命中 → 查 DB
        User user = userConverter.toUser(userMapper.selectByUsername(username));

        // 4. 回填缓存
        if (user != null) {
            userCacheService.putUser(user);
        }
        return user;
    }

    @Override
    public User findByPhone(String phone) {
        if (phone == null) return null;

        // 1. 二级映射缓存：phone → userId
        Long userId = userCacheService.getUserIdByPhone(phone);
        if (userId != null) {
            User cached = userCacheService.getUser(userId);
            if (cached != null) return cached;
        }

        // 2. 缓存未命中 → 查 DB
        User user = userConverter.toUser(userMapper.selectByPhone(phone));

        // 3. 回填缓存
        if (user != null) {
            userCacheService.putUser(user);
        }
        return user;
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;

        // 1. 二级映射缓存：email → userId
        Long userId = userCacheService.getUserIdByEmail(email);
        if (userId != null) {
            User cached = userCacheService.getUser(userId);
            if (cached != null) return cached;
        }

        // 2. 缓存未命中 → 查 DB
        User user = userConverter.toUser(userMapper.selectByEmail(email));

        // 3. 回填缓存
        if (user != null) {
            userCacheService.putUser(user);
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        if (id == null) return null;

        // 1. 一级缓存：user:info:{id}
        User cached = userCacheService.getUser(id);
        if (cached != null) return cached;

        // 2. 缓存未命中 → 查 DB
        User user = userConverter.toUser(userMapper.selectById(id));

        // 3. 回填缓存
        if (user != null) {
            userCacheService.putUser(user);
        }
        return user;
    }

    @Override
    public User findByOpenId(String openId) {
        if (openId == null) return null;

        // 1. 二级映射缓存：openId → userId
        Long userId = userCacheService.getUserIdByOpenId(openId);
        if (userId != null) {
            User cached = userCacheService.getUser(userId);
            if (cached != null) return cached;
        }

        // 2. 缓存未命中 → 查 DB
        User user = userConverter.toUser(userMapper.selectByOpenId(openId));

        // 3. 回填缓存
        if (user != null) {
            userCacheService.putUser(user);
        }
        return user;
    }

    // ==================== 存在性检查（轻量：只查映射缓存 → DB） ====================

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;
        // 先查映射缓存
        if (userCacheService.getUserIdByUsername(username) != null) return true;
        // 缓存未命中 → 查 DB
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null) return false;
        if (userCacheService.getUserIdByPhone(phone) != null) return true;
        return userMapper.countByPhone(phone) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        if (userCacheService.getUserIdByEmail(email) != null) return true;
        return userMapper.countByEmail(email) > 0;
    }

    // ==================== 管理员查询（不走缓存） ====================

    @Override
    public java.util.List<User> findUsers(String keyword, Integer status, Integer userType, int offset, int size) {
        return userMapper.selectPage(keyword, status, userType, offset, size)
                .stream().map(userConverter::toUser).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long countUsers(String keyword, Integer status, Integer userType) {
        return userMapper.countPage(keyword, status, userType);
    }
}
