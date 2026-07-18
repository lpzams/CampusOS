package com.campus.domain.user.repository;

import com.campus.domain.user.entity.UserProfile;

public interface UserProfileRepository {

    void save(UserProfile profile);

    UserProfile findByUserId(Long userId);
}