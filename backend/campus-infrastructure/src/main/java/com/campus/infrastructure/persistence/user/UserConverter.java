package com.campus.infrastructure.persistence.user;

import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.entity.UserRole;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    // ===== User 转换 =====

    public User toUser(UserPO po) {
        if (po == null) return null;
        User user = new User();
        BeanUtils.copyProperties(po, user);
        return user;
    }

    public UserPO toUserPO(User user) {
        if (user == null) return null;
        UserPO po = new UserPO();
        BeanUtils.copyProperties(user, po);
        return po;
    }

    // ===== UserProfile 转换 =====

    public UserProfile toUserProfile(UserProfilePO po) {
        if (po == null) return null;
        UserProfile profile = new UserProfile();
        BeanUtils.copyProperties(po, profile);
        return profile;
    }

    public UserProfilePO toUserProfilePO(UserProfile profile) {
        if (profile == null) return null;
        UserProfilePO po = new UserProfilePO();
        BeanUtils.copyProperties(profile, po);
        return po;
    }

    // ===== UserRole 转换 =====
    public UserRolePO toUserRolePo(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        UserRolePO po = new UserRolePO();
        BeanUtils.copyProperties(userRole, po);
        return po;
    }

    // ===== UserRole 转换 =====

    public UserRole toUserRole(UserRolePO po) {
        if (po == null) return null;
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(po, userRole);
        return userRole;
    }

    public UserRolePO toUserRolePO(UserRole userRole) {
        if (userRole == null) return null;
        UserRolePO po = new UserRolePO();
        BeanUtils.copyProperties(userRole, po);
        return po;
    }

    public List<UserRole> toUserRoleList(List<UserRolePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return new ArrayList<>();
        }
        return poList.stream()
                .map(this::toUserRole)
                .collect(Collectors.toList());
    }

    public List<UserRolePO> toUserRolePOList(List<UserRole> userRoleList) {
        if (userRoleList == null || userRoleList.isEmpty()) {
            return new ArrayList<>();
        }
        return userRoleList.stream()
                .map(this::toUserRolePO)
                .collect(Collectors.toList());
    }
}