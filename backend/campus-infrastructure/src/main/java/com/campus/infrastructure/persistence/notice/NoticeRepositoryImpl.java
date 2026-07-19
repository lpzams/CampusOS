package com.campus.infrastructure.persistence.notice;

import com.campus.domain.notice.entity.Notice;
import com.campus.domain.notice.repository.NoticeRepository;
import com.campus.infrastructure.cache.NoticeCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 公告仓储实现 —— Cache-Aside 模式（详情缓存，列表直接查DB）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final NoticeMapper noticeMapper;
    private final NoticeConverter noticeConverter;
    private final NoticeCacheService noticeCacheService;

    @Override
    public List<Notice> findPage(String type, String department, int offset, int size) {
        return noticeConverter.toNoticeList(
                noticeMapper.selectPage(type, department, offset, size));
    }

    @Override
    public long count(String type, String department) {
        return noticeMapper.count(type, department);
    }

    @Override
    public Notice findById(Long id) {
        if (id == null) return null;

        // 1. 查缓存
        Notice cached = noticeCacheService.getNotice(id);
        if (cached != null) return cached;

        // 2. 查 DB
        Notice notice = noticeConverter.toNotice(noticeMapper.selectById(id));

        // 3. 回填缓存
        if (notice != null) {
            noticeCacheService.putNotice(notice);
        }
        return notice;
    }

    @Override
    public long countByType(String type) {
        return noticeMapper.countByType(type);
    }

    @Override
    public void incrementReadCount(Long id) {
        noticeMapper.incrementReadCount(id);
        noticeCacheService.evictNotice(id);
    }

    // ==================== 写操作（DB 优先 → 删缓存） ====================

    @Override
    public void save(Notice notice) {
        NoticePO po = noticeConverter.toNoticePO(notice);
        noticeMapper.insert(po);
        notice.setId(po.getId());
    }

    @Override
    public void update(Notice notice) {
        NoticePO po = noticeConverter.toNoticePO(notice);
        noticeMapper.updateById(po);
        noticeCacheService.evictNotice(notice.getId());
    }

    @Override
    public void delete(Long id) {
        noticeMapper.deleteById(id);
        noticeCacheService.evictNotice(id);
    }
}
