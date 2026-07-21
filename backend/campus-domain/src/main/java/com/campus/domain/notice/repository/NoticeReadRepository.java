package com.campus.domain.notice.repository;

import com.campus.domain.notice.entity.NoticeRead;

import java.util.List;

public interface NoticeReadRepository {

    /** 查用户是否已读某公告 */
    NoticeRead findByUserIdAndNoticeId(Long userId, Long noticeId);

    /** 查用户已读的公告ID集合 */
    List<Long> findReadNoticeIdsByUserId(Long userId);

    /** 查用户未读的学校公告数 */
    long countUnreadByType(Long userId, String type);

    /** 查用户未读公告总数 */
    long countUnread(Long userId);

    /** 保存已读记录 */
    void save(NoticeRead record);
}
