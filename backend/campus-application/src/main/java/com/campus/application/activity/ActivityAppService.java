package com.campus.application.activity;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.common.api.PageResult;
import com.campus.common.exception.BusinessException;
import com.campus.domain.activity.Activity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ActivityAppService {
    private final CampusAppService records;
    public ActivityAppService(CampusAppService records) { this.records = records; }

    public PageResult<Map<String, Object>> page(String category, String status, int page, int size) {
        List<Map<String, Object>> rows = records.list("activity").stream()
                .filter(a -> category == null || category.isBlank() || category.equals(a.get("categoryCode")))
                .filter(a -> status == null || status.isBlank() || status.equals(a.get("statusCode")) || status.equals(a.get("status"))).toList();
        int safePage = Math.max(1, page), safeSize = Math.min(100, Math.max(1, size));
        int from = Math.min((safePage - 1) * safeSize, rows.size());
        return new PageResult<>(safePage, safeSize, rows.size(), rows.subList(from, Math.min(from + safeSize, rows.size())));
    }

    public Map<String, Object> detail(Long id) { return records.get("activity", id); }

    @Transactional
    public Map<String, Object> register(Long userId, Long activityId) {
        if (records.listExact("activityRegistration", "userId", userId).stream()
                .anyMatch(r -> activityId.equals(Values.longValue(r.get("activityId")))))
            throw new BusinessException(400, "不能重复报名");
        Map<String, Object> row = records.get("activity", activityId);
        Activity activity;
        try {
            activity = new Activity(Values.intValue(row.getOrDefault("maxParticipants", 1)),
                    Values.intValue(row.getOrDefault("currentParticipants", 0)));
            activity.register();
        } catch (RuntimeException e) { throw Values.invalid(e); }
        records.update("activity", activityId, Map.of("currentParticipants", activity.participants()));
        return records.create("activityRegistration", Values.owned(userId, Map.of("activityId", activityId, "status", "已报名")));
    }

    @Transactional
    public void cancel(Long userId, Long registrationId) {
        Map<String, Object> registration = records.get("activityRegistration", registrationId);
        if (!userId.equals(Values.longValue(registration.get("userId")))) throw new BusinessException(403, "无权取消该报名");
        Long activityId = Values.longValue(registration.get("activityId"));
        Map<String, Object> row = records.get("activity", activityId);
        Activity activity = new Activity(Values.intValue(row.get("maxParticipants")), Values.intValue(row.get("currentParticipants")));
        activity.cancel();
        records.update("activity", activityId, Map.of("currentParticipants", activity.participants()));
        records.delete("activityRegistration", registrationId);
    }

    @Transactional
    public Map<String, Object> checkIn(Long userId, Long activityId, String code) {
        if (code == null || code.isBlank()) throw new BusinessException(400, "签到码不能为空");
        Map<String, Object> registration = records.listExact("activityRegistration", "userId", userId).stream()
                .filter(r -> activityId.equals(Values.longValue(r.get("activityId")))).findFirst()
                .orElseThrow(() -> new BusinessException(400, "请先报名活动"));
        return records.update("activityRegistration", Values.longValue(registration.get("id")), Map.of("checkedIn", true));
    }

    public List<Map<String, Object>> mine(Long userId) { return records.listExact("activityRegistration", "userId", userId); }
}
