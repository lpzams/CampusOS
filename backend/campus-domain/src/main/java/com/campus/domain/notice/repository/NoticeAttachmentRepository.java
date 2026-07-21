package com.campus.domain.notice.repository;

import com.campus.domain.notice.entity.NoticeAttachment;

import java.util.List;

public interface NoticeAttachmentRepository {

    /** 按公告ID查附件列表 */
    List<NoticeAttachment> findByNoticeId(Long noticeId);

    /** 批量保存附件 */
    void saveBatch(Long noticeId, List<NoticeAttachment> attachments);

    /** 按公告ID删除所有附件 */
    void deleteByNoticeId(Long noticeId);
}
