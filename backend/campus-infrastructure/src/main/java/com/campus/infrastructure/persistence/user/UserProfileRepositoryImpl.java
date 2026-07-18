package com.campus.infrastructure.persistence.user;

import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfileMapper userProfileMapper;
    private final UserConverter userConverter;

    @Override
    public void save(UserProfile profile) {
        UserProfilePO po = userConverter.toUserProfilePO(profile);
        if (po.getId() == null) {
            userProfileMapper.insert(po);
            profile.setId(po.getId());
        } else {
            userProfileMapper.updateById(po);
        }
    }

    @Override
    public UserProfile findByUserId(Long userId) {
        return userConverter.toUserProfile(userProfileMapper.selectByUserId(userId));
    }
}