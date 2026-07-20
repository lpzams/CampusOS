package com.campus.domain.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ActivityRegistration {
    private Long id;
    private Long activityId;
    private Long userId;
    private String status;
    private Integer checkinStatus;
    private String checkinCode;
    private LocalDateTime checkinTime;
    private String remark;
    private LocalDateTime registerTime;

    // ===== 报名状态常量 =====
    public static final String STATUS_REGISTERED = "REGISTERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // ===== 签到状态 =====
    public static final int CHECKIN_NO = 0;
    public static final int CHECKIN_YES = 1;

    public String getStatusDesc() {
        if (STATUS_REGISTERED.equals(status)) return "已报名";
        if (STATUS_CANCELLED.equals(status)) return "已取消";
        return status;
    }

    // ===== 工厂方法 =====

    public static ActivityRegistration create(Long activityId, Long userId, String remark) {
        ActivityRegistration r = new ActivityRegistration();
        r.setActivityId(activityId);
        r.setUserId(userId);
        r.setStatus(STATUS_REGISTERED);
        r.setCheckinStatus(CHECKIN_NO);
        r.setCheckinCode(generateCheckinCode());
        r.setRemark(remark);
        r.setRegisterTime(LocalDateTime.now());
        return r;
    }

    /** 签到 */
    public void checkin() {
        this.checkinStatus = CHECKIN_YES;
        this.checkinTime = LocalDateTime.now();
    }

    /** 取消报名 */
    public void cancel() {
        this.status = STATUS_CANCELLED;
    }

    /** 生成6位随机签到码 */
    private static String generateCheckinCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
