package com.campus.infrastructure.persistence.notice;

import com.campus.domain.notice.entity.NoticeRead;
import com.campus.domain.notice.repository.NoticeReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NoticeReadRepositoryImpl implements NoticeReadRepository {

    private final NoticeReadMapper noticeReadMapper;
    private final NoticeConverter noticeConverter;

    @Override
    public NoticeRead findByUserIdAndNoticeId(Long userId, Long noticeId) {
        if (userId == null || noticeId == null) return null;
        return noticeConverter.toNoticeRead(
                noticeReadMapper.selectByUserIdAndNoticeId(userId, noticeId));
    }

    @Override
    public List<Long> findReadNoticeIdsByUserId(Long userId) {
        if (userId == null) return Collections.emptyList();
        List<Long> ids = noticeReadMapper.selectReadNoticeIdsByUserId(userId);
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public long countUnreadByType(Long userId, String type) {
        if (userId == null || type == null) return 0;
        return noticeReadMapper.countUnreadByType(userId, type);
    }

    @Override
    public long countUnread(Long userId) {
        if (userId == null) return 0;
        return noticeReadMapper.countUnread(userId);
    }

    @Override
    public void save(NoticeRead record) {
        NoticeReadPO po = noticeConverter.toNoticeReadPO(record);
        noticeReadMapper.insert(po);
        record.setId(po.getId());
    }
}
