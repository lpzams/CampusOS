package com.campus.domain.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    private String title;
    private String category;
    private String coverImage;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String organizer;
    private String contactPhone;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 活动状态常量 =====
    public static final String STATUS_UPCOMING = "UPCOMING";
    public static final String STATUS_ONGOING = "ONGOING";
    public static final String STATUS_ENDED = "ENDED";

    // ===== 分类常量 =====
    public static final String CATEGORY_SPORTS = "SPORTS";
    public static final String CATEGORY_CULTURE = "CULTURE";
    public static final String CATEGORY_ACADEMIC = "ACADEMIC";
    public static final String CATEGORY_VOLUNTEER = "VOLUNTEER";

    // ===== 状态描述 =====
    public String getStatusDesc() {
        switch (status) {
            case STATUS_UPCOMING: return "报名中";
            case STATUS_ONGOING: return "进行中";
            case STATUS_ENDED: return "已结束";
            default: return status;
        }
    }

    public String getCategoryDesc() {
        if (CATEGORY_SPORTS.equals(category)) return "体育";
        if (CATEGORY_CULTURE.equals(category)) return "文艺";
        if (CATEGORY_ACADEMIC.equals(category)) return "学术";
        if (CATEGORY_VOLUNTEER.equals(category)) return "志愿";
        return category;
    }

    /** 是否可报名：即将开始且未满员 */
    public boolean canRegister() {
        return STATUS_UPCOMING.equals(status)
                && currentParticipants != null
                && maxParticipants != null
                && currentParticipants < maxParticipants;
    }

    // ===== 工厂方法 =====

    public static Activity create(String title, String category, String content,
                                   String coverImage, LocalDateTime startTime, LocalDateTime endTime,
                                   String location, Integer maxParticipants,
                                   String organizer, String contactPhone) {
        Activity a = new Activity();
        a.setTitle(title);
        a.setCategory(category);
        a.setContent(content);
        a.setCoverImage(coverImage);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        a.setLocation(location);
        a.setMaxParticipants(maxParticipants);
        a.setCurrentParticipants(0);
        a.setOrganizer(organizer);
        a.setContactPhone(contactPhone);
        a.setStatus(STATUS_UPCOMING);
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        return a;
    }

    // ===== 领域方法 =====

    /** 增加报名人数 */
    public void incrementParticipants() {
        if (this.currentParticipants == null) this.currentParticipants = 0;
        this.currentParticipants++;
    }

    /** 减少报名人数 */
    public void decrementParticipants() {
        if (this.currentParticipants != null && this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    /** 更新状态（根据当前时间自动判断） */
    public void refreshStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            this.status = STATUS_UPCOMING;
        } else if (now.isAfter(endTime)) {
            this.status = STATUS_ENDED;
        } else {
            this.status = STATUS_ONGOING;
        }
    }

    /** 管理员更新部分字段 */
    public void updateFields(String title, String category, String content,
                              String coverImage, LocalDateTime startTime, LocalDateTime endTime,
                              String location, Integer maxParticipants,
                              String organizer, String contactPhone) {
        if (title != null) this.title = title;
        if (category != null) this.category = category;
        if (content != null) this.content = content;
        if (coverImage != null) this.coverImage = coverImage;
        if (startTime != null) this.startTime = startTime;
        if (endTime != null) this.endTime = endTime;
        if (location != null) this.location = location;
        if (maxParticipants != null) this.maxParticipants = maxParticipants;
        if (organizer != null) this.organizer = organizer;
        if (contactPhone != null) this.contactPhone = contactPhone;
        this.updateTime = LocalDateTime.now();
    }
}
