package com.campus.infrastructure.persistence.user;

import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.infrastructure.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 用户详情仓储实现 —— Cache-Aside 模式
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfileMapper userProfileMapper;
    private final UserConverter userConverter;
    private final UserCacheService userCacheService;

    @Override
    public void save(UserProfile profile) {
        UserProfilePO po = userConverter.toUserProfilePO(profile);
        if (po.getId() == null) {
            userProfileMapper.insert(po);
            profile.setId(po.getId());
        } else {
            userProfileMapper.updateById(po);
        }
        // 新增或更新 → 删缓存
        if (profile.getUserId() != null) {
            userCacheService.evictProfile(profile.getUserId());
        }
    }

    @Override
    public UserProfile findByUserId(Long userId) {
        // 1. 查缓存
        UserProfile cached = userCacheService.getProfile(userId);
        if (cached != null) return cached;

        // 2. 查 DB
        UserProfile profile = userConverter.toUserProfile(
                userProfileMapper.selectByUserId(userId));

        // 3. 回填缓存
        if (profile != null) {
            userCacheService.putProfile(profile);
        }
        return profile;
    }

    @Override
    public void update(UserProfile profile) {
        // 1. 先写 DB
        UserProfilePO po = userConverter.toUserProfilePO(profile);
        userProfileMapper.updateById(po);
        // 2. 再删缓存
        if (profile.getUserId() != null) {
            userCacheService.evictProfile(profile.getUserId());
        }
    }

    @Override
    public java.util.List<UserProfile> findByUserIds(java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return java.util.Collections.emptyList();
        return userProfileMapper.selectByUserIds(userIds)
                .stream().map(userConverter::toUserProfile).collect(java.util.stream.Collectors.toList());
    }
}
