package com.campus.domain.user.repository;

import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;

public interface UserRepository {

    void save(User user);

    void save(User user, UserProfile profile, UserRole role);

    User findByUsername(String username);

    User findByPhone(String phone);

    User findByEmail(String email);

    User findById(Long id);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    void update(User user);

    User findByOpenId(String openId);
}