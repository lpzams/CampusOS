package com.campus.application.activity.service;

import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.activity.entity.Activity;
import com.campus.domain.activity.entity.ActivityRegistration;
import com.campus.domain.activity.repository.ActivityRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityAppService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 13.1 获取活动列表 ====================

    public PageResult<Map<String, Object>> getActivityList(String category, String status,
                                                            int page, int size, Long currentUserId) {
        int offset = (page - 1) * size;
        List<Activity> activities = activityRepository.findList(category, status, offset, size);
        long total = activityRepository.countList(category, status);

        // 刷新状态（根据时间自动判断活动是否变更状态）
        refreshActivitiesStatus(activities);

        // 获取当前用户已报名的活动ID列表
        Set<Long> registeredIds = Collections.emptySet();
        if (currentUserId != null) {
            registeredIds = new HashSet<>(activityRepository.findRegisteredActivityIds(currentUserId));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Activity a : activities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("category", a.getCategoryDesc());
            item.put("categoryCode", a.getCategory());
            item.put("coverImage", a.getCoverImage());
            item.put("startTime", a.getStartTime() != null ? a.getStartTime().format(FORMATTER) : null);
            item.put("endTime", a.getEndTime() != null ? a.getEndTime().format(FORMATTER) : null);
            item.put("location", a.getLocation());
            item.put("maxParticipants", a.getMaxParticipants());
            item.put("currentParticipants", a.getCurrentParticipants());
            item.put("status", a.getStatusDesc());
            item.put("statusCode", a.getStatus());
            item.put("isRegistered", currentUserId != null && registeredIds.contains(a.getId()));
            item.put("createTime", a.getCreateTime() != null ? a.getCreateTime().format(FORMATTER) : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 13.2 获取活动详情 ====================

    public Map<String, Object> getActivityDetail(Long id, Long currentUserId) {
        Activity activity = activityRepository.findById(id);
        if (activity == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        activity.refreshStatus();

        boolean isRegistered = false;
        if (currentUserId != null) {
            isRegistered = activityRepository.existsRegistration(id, currentUserId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", activity.getId());
        result.put("title", activity.getTitle());
        result.put("category", activity.getCategoryDesc());
        result.put("categoryCode", activity.getCategory());
        result.put("coverImage", activity.getCoverImage());
        result.put("content", activity.getContent());
        result.put("startTime", activity.getStartTime() != null ? activity.getStartTime().format(FORMATTER) : null);
        result.put("endTime", activity.getEndTime() != null ? activity.getEndTime().format(FORMATTER) : null);
        result.put("location", activity.getLocation());
        result.put("maxParticipants", activity.getMaxParticipants());
        result.put("currentParticipants", activity.getCurrentParticipants());
        result.put("status", activity.getStatusDesc());
        result.put("statusCode", activity.getStatus());
        result.put("isRegistered", isRegistered);
        result.put("organizer", activity.getOrganizer());
        result.put("contactPhone", activity.getContactPhone());
        result.put("createTime", activity.getCreateTime() != null ? activity.getCreateTime().format(FORMATTER) : null);
        result.put("updateTime", activity.getUpdateTime() != null ? activity.getUpdateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 13.3 报名活动 ====================

    @Transactional
    public Map<String, Object> registerActivity(Long userId, Map<String, Object> command) {
        Long activityId = ((Number) command.get("activityId")).longValue();
        String remark = (String) command.get("remark");

        Activity activity = activityRepository.findById(activityId);
        if (activity == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        activity.refreshStatus();

        if (Activity.STATUS_ENDED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.ACTIVITY_ENDED);
        }
        if (!activity.canRegister()) {
            throw new BusinessException(ResultCode.ACTIVITY_FULL);
        }

        if (activityRepository.existsRegistration(activityId, userId)) {
            throw new BusinessException(ResultCode.ACTIVITY_ALREADY_REGISTERED);
        }

        // 创建报名记录
        ActivityRegistration registration = ActivityRegistration.create(activityId, userId, remark);
        activityRepository.saveRegistration(registration);

        // 更新活动报名人数
        activity.incrementParticipants();
        activityRepository.update(activity);

        log.info("报名活动: activityId={}, userId={}, registerId={}", activityId, userId, registration.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("registerId", registration.getId());
        result.put("activityId", activityId);
        result.put("activityTitle", activity.getTitle());
        result.put("registerTime", registration.getRegisterTime() != null
                ? registration.getRegisterTime().format(FORMATTER) : null);
        result.put("status", registration.getStatusDesc());
        result.put("statusCode", registration.getStatus());
        result.put("checkinCode", registration.getCheckinCode());
        return result;
    }

    // ==================== 13.4 取消报名 ====================

    @Transactional
    public void cancelRegistration(Long userId, Long registrationId) {
        ActivityRegistration registration = activityRepository.findRegistrationById(registrationId);
        if (registration == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_REGISTERED);
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACTIVITY_CANCEL_FAILED.getCode(), "只能取消自己的报名");
        }
        if (ActivityRegistration.STATUS_CANCELLED.equals(registration.getStatus())) {
            throw new BusinessException(ResultCode.ACTIVITY_CANCEL_FAILED.getCode(), "报名已取消");
        }

        registration.cancel();
        activityRepository.updateRegistration(registration);

        // 减少活动报名人数
        Activity activity = activityRepository.findById(registration.getActivityId());
        if (activity != null) {
            activity.decrementParticipants();
            activityRepository.update(activity);
        }

        log.info("取消报名: registrationId={}, userId={}", registrationId, userId);
    }

    // ==================== 13.5 活动签到 ====================

    @Transactional
    public Map<String, Object> checkinActivity(Long userId, Map<String, Object> command) {
        Long activityId = ((Number) command.get("activityId")).longValue();
        String code = (String) command.get("code");

        Activity activity = activityRepository.findById(activityId);
        if (activity == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);

        ActivityRegistration registration = activityRepository.findRegistration(activityId, userId);
        if (registration == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_REGISTERED);
        if (ActivityRegistration.STATUS_CANCELLED.equals(registration.getStatus())) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_REGISTERED);
        }
        if (registration.getCheckinStatus() == ActivityRegistration.CHECKIN_YES) {
            throw new BusinessException(ResultCode.ACTIVITY_ALREADY_CHECKIN);
        }

        // 验证签到码
        if (!registration.getCheckinCode().equalsIgnoreCase(code)) {
            throw new BusinessException(ResultCode.ACTIVITY_CHECKIN_FAILED.getCode(), "签到码错误");
        }

        registration.checkin();
        activityRepository.updateRegistration(registration);

        log.info("活动签到: activityId={}, userId={}, code={}", activityId, userId, code);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activityId", activityId);
        result.put("activityTitle", activity.getTitle());
        result.put("checkinTime", registration.getCheckinTime() != null
                ? registration.getCheckinTime().format(FORMATTER) : null);
        result.put("status", "已签到");
        return result;
    }

    // ==================== 13.6 获取我的活动 ====================

    public List<Map<String, Object>> getMyActivities(Long userId) {
        List<ActivityRegistration> registrations = activityRepository.findRegistrationsByUserId(userId);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ActivityRegistration reg : registrations) {
            Activity activity = activityRepository.findById(reg.getActivityId());
            if (activity == null) continue;
            activity.refreshStatus();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", activity.getId());
            item.put("title", activity.getTitle());
            item.put("category", activity.getCategoryDesc());
            item.put("categoryCode", activity.getCategory());
            item.put("coverImage", activity.getCoverImage());
            item.put("startTime", activity.getStartTime() != null ? activity.getStartTime().format(FORMATTER) : null);
            item.put("endTime", activity.getEndTime() != null ? activity.getEndTime().format(FORMATTER) : null);
            item.put("location", activity.getLocation());
            // 状态：活动已结束则显示"已结束"，否则显示报名状态
            if (Activity.STATUS_ENDED.equals(activity.getStatus())) {
                item.put("status", "已结束");
                item.put("statusCode", "ENDED");
            } else {
                item.put("status", reg.getStatusDesc());
                item.put("statusCode", reg.getStatus());
            }
            item.put("registerTime", reg.getRegisterTime() != null ? reg.getRegisterTime().format(FORMATTER) : null);
            item.put("isCheckin", reg.getCheckinStatus() == ActivityRegistration.CHECKIN_YES);
            list.add(item);
        }

        return list;
    }

    // ==================== 13.7 创建活动（管理员） ====================

    @Transactional
    public Map<String, Object> createActivity(Map<String, Object> command) {
        String title = (String) command.get("title");
        String category = (String) command.get("category");
        String content = (String) command.get("content");
        String coverImage = (String) command.get("coverImage");
        LocalDateTime startTime = LocalDateTime.parse((String) command.get("startTime"), FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse((String) command.get("endTime"), FORMATTER);
        String location = (String) command.get("location");
        Integer maxParticipants = ((Number) command.get("maxParticipants")).intValue();
        String organizer = (String) command.get("organizer");
        String contactPhone = (String) command.get("contactPhone");

        Activity activity = Activity.create(title, category, content, coverImage,
                startTime, endTime, location, maxParticipants, organizer, contactPhone);
        activity.refreshStatus();
        activityRepository.save(activity);

        log.info("创建活动: activityId={}, title={}", activity.getId(), title);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", activity.getId());
        result.put("title", activity.getTitle());
        result.put("category", activity.getCategory());
        result.put("createTime", activity.getCreateTime() != null ? activity.getCreateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 13.8 更新活动（管理员） ====================

    @Transactional
    public Map<String, Object> updateActivity(Long id, Map<String, Object> command) {
        Activity activity = activityRepository.findById(id);
        if (activity == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (command.get("startTime") != null) {
            startTime = LocalDateTime.parse((String) command.get("startTime"), FORMATTER);
        }
        if (command.get("endTime") != null) {
            endTime = LocalDateTime.parse((String) command.get("endTime"), FORMATTER);
        }
        Integer maxParticipants = command.get("maxParticipants") != null
                ? ((Number) command.get("maxParticipants")).intValue() : null;

        activity.updateFields(
                (String) command.get("title"),
                (String) command.get("category"),
                (String) command.get("content"),
                (String) command.get("coverImage"),
                startTime, endTime,
                (String) command.get("location"),
                maxParticipants,
                (String) command.get("organizer"),
                (String) command.get("contactPhone")
        );
        activity.refreshStatus();
        activityRepository.update(activity);

        log.info("更新活动: activityId={}", id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", activity.getId());
        result.put("title", activity.getTitle());
        result.put("updateTime", activity.getUpdateTime() != null ? activity.getUpdateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 13.9 删除活动（管理员） ====================

    @Transactional
    public void deleteActivity(Long id) {
        Activity activity = activityRepository.findById(id);
        if (activity == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        activityRepository.deleteById(id);
        log.info("删除活动: activityId={}", id);
    }

    // ==================== 13.10 获取活动列表（管理员） ====================

    public PageResult<Map<String, Object>> getAdminActivityList(String category, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<Activity> activities = activityRepository.findAdminList(category, status, offset, size);
        long total = activityRepository.countAdminList(category, status);
        refreshActivitiesStatus(activities);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Activity a : activities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("category", a.getCategoryDesc());
            item.put("categoryCode", a.getCategory());
            item.put("location", a.getLocation());
            item.put("startTime", a.getStartTime() != null ? a.getStartTime().format(FORMATTER) : null);
            item.put("maxParticipants", a.getMaxParticipants());
            item.put("currentParticipants", a.getCurrentParticipants());
            item.put("status", a.getStatus());
            item.put("statusDesc", a.getStatusDesc());
            item.put("createTime", a.getCreateTime() != null ? a.getCreateTime().format(FORMATTER) : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 13.11 活动报名列表（管理员） ====================

    public PageResult<Map<String, Object>> getActivityRegistrations(Long activityId, int page, int size) {
        Activity activity = activityRepository.findById(activityId);
        if (activity == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);

        int offset = (page - 1) * size;
        List<ActivityRegistration> registrations = activityRepository.findRegistrationsByActivityId(activityId, offset, size);
        long total = activityRepository.countRegistrationsByActivityId(activityId);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ActivityRegistration reg : registrations) {
            User user = userRepository.findById(reg.getUserId());
            UserProfile profile = userProfileRepository.findByUserId(reg.getUserId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", reg.getUserId());
            item.put("realName", user != null ? user.getRealName() : null);
            item.put("studentId", profile != null ? profile.getStudentId() : null);
            item.put("phone", user != null ? user.getPhone() : null);
            item.put("registerTime", reg.getRegisterTime() != null ? reg.getRegisterTime().format(FORMATTER) : null);
            item.put("status", reg.getStatusDesc());
            item.put("statusCode", reg.getStatus());
            item.put("isCheckin", reg.getCheckinStatus() == ActivityRegistration.CHECKIN_YES);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ===== 私有方法 =====

    private void refreshActivitiesStatus(List<Activity> activities) {
        for (Activity a : activities) {
            a.refreshStatus();
        }
    }
}
