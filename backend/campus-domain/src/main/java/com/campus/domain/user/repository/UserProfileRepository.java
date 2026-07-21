package com.campus.domain.user.repository;

import com.campus.domain.user.entity.UserProfile;

public interface UserProfileRepository {

    void save(UserProfile profile);

    UserProfile findByUserId(Long userId);

    void update(UserProfile profile);

    java.util.List<UserProfile> findByUserIds(java.util.List<Long> userIds);
}