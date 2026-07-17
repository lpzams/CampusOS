package com.campus.infrastructure.persistence.user;

import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import com.campus.domain.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserConverter userConverter;

    @Override
    public void save(User user) {
        UserPO po = userConverter.toUserPO(user);
        if (po.getId() == null) {
            userMapper.insert(po);
            user.setId(po.getId());
        } else {
            userMapper.updateById(po);
        }
    }

    @Override
    @Transactional
    public void save(User user, UserProfile profile, UserRole role) {
        log.info("保存用户：{}", user);
        // 1. 插入 user 表（生成 id）
        UserPO userPO = userConverter.toUserPO(user);
        userMapper.insert(userPO);
        Long userId = userPO.getId();

        // 2. 回填 id 到 Domain 实体
        user.setId(userId);

        // 3. 插入 profile（用 userId 关联）
        if (profile != null) {
            profile.setUserId(userId);  // ← Repository 设置 userId
            UserProfilePO profilePO = userConverter.toUserProfilePO(profile);
            userProfileMapper.insert(profilePO);
        }
        log.info("保存用户信息：{}",profile);

        //创建角色
        role = UserRole.createDefaultRole(userId, user.getUserType());
        log.info("保存用户信息：{}",role);
        // 4. 插入 role（用 userId 关联）
        if (role != null) {
            role.setUserId(userId);  // ← Repository 设置 userId
            UserRolePO rolePO = userConverter.toUserRolePO(role);
            userRoleMapper.insert(rolePO);
        }
    }

    @Override
    public User findByUsername(String username) {
        return userConverter.toUser(userMapper.selectByUsername(username));
    }

    @Override
    public User findByPhone(String phone) {
        return userConverter.toUser(userMapper.selectByPhone(phone));
    }

    @Override
    public User findByEmail(String email) {
        return userConverter.toUser(userMapper.selectByEmail(email));
    }

    @Override
    public User findById(Long id) {
        return userConverter.toUser(userMapper.selectById(id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.countByPhone(phone) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }
}