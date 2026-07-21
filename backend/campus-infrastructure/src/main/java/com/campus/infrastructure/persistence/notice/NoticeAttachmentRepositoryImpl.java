package com.campus.infrastructure.persistence.notice;

import com.campus.domain.notice.entity.NoticeAttachment;
import com.campus.domain.notice.repository.NoticeAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NoticeAttachmentRepositoryImpl implements NoticeAttachmentRepository {

    private final NoticeAttachmentMapper noticeAttachmentMapper;
    private final NoticeConverter noticeConverter;

    @Override
    public List<NoticeAttachment> findByNoticeId(Long noticeId) {
        if (noticeId == null) return Collections.emptyList();
        return noticeConverter.toNoticeAttachmentList(
                noticeAttachmentMapper.selectByNoticeId(noticeId));
    }

    @Override
    public void saveBatch(Long noticeId, List<NoticeAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) return;
        for (NoticeAttachment att : attachments) {
            att.setNoticeId(noticeId);
            NoticeAttachmentPO po = noticeConverter.toNoticeAttachmentPO(att);
            noticeAttachmentMapper.insert(po);
        }
    }

    @Override
    public void deleteByNoticeId(Long noticeId) {
        if (noticeId == null) return;
        noticeAttachmentMapper.deleteByNoticeId(noticeId);
    }
}
