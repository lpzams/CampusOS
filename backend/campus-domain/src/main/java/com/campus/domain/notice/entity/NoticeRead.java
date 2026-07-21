package com.campus.domain.notice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeRead {

    private Long id;
    private Long userId;
    private Long noticeId;
    private LocalDateTime readTime;

    // ========== 工厂方法 ==========

    public static NoticeRead create(Long userId, Long noticeId) {
        NoticeRead record = new NoticeRead();
        record.setUserId(userId);
        record.setNoticeId(noticeId);
        record.setReadTime(LocalDateTime.now());
        return record;
    }
}
