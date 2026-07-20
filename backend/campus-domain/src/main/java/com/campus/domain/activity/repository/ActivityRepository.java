package com.campus.domain.activity.repository;

import com.campus.domain.activity.entity.Activity;
import com.campus.domain.activity.entity.ActivityRegistration;

import java.util.List;

public interface ActivityRepository {

    // ===== Activity CRUD =====
    void save(Activity activity);
    void update(Activity activity);
    Activity findById(Long id);
    void deleteById(Long id);

    // 公开列表
    List<Activity> findList(String category, String status, int offset, int size);
    long countList(String category, String status);

    // 管理员列表
    List<Activity> findAdminList(String category, String status, int offset, int size);
    long countAdminList(String category, String status);

    // ===== Registration =====
    void saveRegistration(ActivityRegistration registration);
    void updateRegistration(ActivityRegistration registration);
    ActivityRegistration findRegistrationById(Long id);
    ActivityRegistration findRegistration(Long activityId, Long userId);
    List<ActivityRegistration> findRegistrationsByActivityId(Long activityId, int offset, int size);
    long countRegistrationsByActivityId(Long activityId);

    // 我报名的活动ID列表
    List<Long> findRegisteredActivityIds(Long userId);
    // 我报名的详情列表
    List<ActivityRegistration> findRegistrationsByUserId(Long userId);

    // 检查是否已报名
    boolean existsRegistration(Long activityId, Long userId);
}
