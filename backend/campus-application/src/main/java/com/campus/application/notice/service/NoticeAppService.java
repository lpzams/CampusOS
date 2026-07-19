package com.campus.application.notice.service;

import com.campus.application.notice.command.NoticeListQuery;
import com.campus.application.notice.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.context.LoginUser;
import com.campus.common.context.LoginUserHolder;
import com.campus.common.exception.BusinessException;
import com.campus.domain.notice.entity.Notice;
import com.campus.domain.notice.entity.NoticeAttachment;
import com.campus.domain.notice.entity.NoticeRead;
import com.campus.domain.notice.repository.NoticeAttachmentRepository;
import com.campus.domain.notice.repository.NoticeReadRepository;
import com.campus.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeAppService {

    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 4.1 获取公告列表 ====================

    public PageResult<NoticeListDTO> getNoticeList(NoticeListQuery query) {
        // 1. 分页查公告
        List<Notice> notices = noticeRepository.findPage(
                query.getType(), query.getDepartment(), query.getOffset(), query.getPageSize());
        long total = noticeRepository.count(query.getType(), query.getDepartment());

        // 2. 获取当前用户已读的公告ID集合（用于 isRead 标记）
        Set<Long> readNoticeIds = getReadNoticeIds();

        // 3. 拼装 DTO
        List<NoticeListDTO> dtoList = notices.stream()
                .map(notice -> NoticeListDTO.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .type(notice.getType())
                        .typeDesc(notice.getTypeDesc())
                        .department(notice.getDepartment())
                        .summary(notice.getSummary())
                        .isTop(notice.getIsTop() != null && notice.getIsTop() == 1)
                        .isRead(readNoticeIds.contains(notice.getId()))
                        .createTime(notice.getCreateTime() != null ? notice.getCreateTime().format(FORMATTER) : null)
                        .deadline(notice.getDeadline() != null ? notice.getDeadline().format(FORMATTER) : null)
                        .build())
                .collect(Collectors.toList());

        return PageResult.of(total, dtoList, query.getPageNum(), query.getPageSize());
    }

    // ==================== 4.2 获取公告详情 ====================

    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailDTO getNoticeDetail(Long noticeId) {
        // 1. 查公告
        Notice notice = noticeRepository.findById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        }

        // 2. 阅读数 +1
        noticeRepository.incrementReadCount(noticeId);

        // 3. 查附件
        List<NoticeAttachment> attachments = noticeAttachmentRepository.findByNoticeId(noticeId);
        List<NoticeAttachmentDTO> attachmentDTOs = attachments != null ? attachments.stream()
                .map(att -> NoticeAttachmentDTO.builder()
                        .id(att.getId())
                        .name(att.getName())
                        .url(att.getUrl())
                        .size(att.getSize())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        // 4. 判断当前用户是否已读
        boolean isRead = false;
        try {
            Long userId = getCurrentUserIdOrNull();
            if (userId != null) {
                NoticeRead record = noticeReadRepository.findByUserIdAndNoticeId(userId, noticeId);
                isRead = (record != null);
            }
        } catch (Exception ignored) {
        }

        // 5. 拼装 DTO
        return NoticeDetailDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .type(notice.getType())
                .typeDesc(notice.getTypeDesc())
                .department(notice.getDepartment())
                .isTop(notice.getIsTop() != null && notice.getIsTop() == 1)
                .isRead(isRead)
                .readCount(notice.getReadCount())
                .createTime(notice.getCreateTime() != null ? notice.getCreateTime().format(FORMATTER) : null)
                .updateTime(notice.getUpdateTime() != null ? notice.getUpdateTime().format(FORMATTER) : null)
                .deadline(notice.getDeadline() != null ? notice.getDeadline().format(FORMATTER) : null)
                .attachments(attachmentDTOs)
                .build();
    }

    // ==================== 4.3 标记公告已读 ====================

    public NoticeReadResponseDTO markAsRead(Long userId, Long noticeId) {
        // 1. 校验公告存在
        Notice notice = noticeRepository.findById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        }

        // 2. 校验是否已读（幂等）
        NoticeRead existing = noticeReadRepository.findByUserIdAndNoticeId(userId, noticeId);
        if (existing != null) {
            // 已读：直接返回之前的阅读时间
            return NoticeReadResponseDTO.builder()
                    .noticeId(noticeId)
                    .readTime(existing.getReadTime() != null ? existing.getReadTime().format(FORMATTER) : null)
                    .build();
        }

        // 3. 创建已读记录
        NoticeRead record = NoticeRead.create(userId, noticeId);
        noticeReadRepository.save(record);

        log.info("公告已读: userId={}, noticeId={}", userId, noticeId);

        return NoticeReadResponseDTO.builder()
                .noticeId(noticeId)
                .readTime(record.getReadTime().format(FORMATTER))
                .build();
    }

    // ==================== 4.4 获取未读公告数量 ====================

    public NoticeUnreadCountDTO getUnreadCount(Long userId) {
        long totalUnread = noticeReadRepository.countUnread(userId);
        long schoolUnread = noticeReadRepository.countUnreadByType(userId, Notice.TYPE_SCHOOL);
        long deptUnread = noticeReadRepository.countUnreadByType(userId, Notice.TYPE_DEPT);

        return NoticeUnreadCountDTO.builder()
                .total(totalUnread)
                .school(schoolUnread)
                .department(deptUnread)
                .build();
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前用户已读的公告ID集合（未登录返回空集合）
     */
    private Set<Long> getReadNoticeIds() {
        try {
            Long userId = getCurrentUserIdOrNull();
            if (userId != null) {
                List<Long> ids = noticeReadRepository.findReadNoticeIdsByUserId(userId);
                return ids != null ? Set.copyOf(ids) : Collections.emptySet();
            }
        } catch (Exception ignored) {
        }
        return Collections.emptySet();
    }

    private Long getCurrentUserIdOrNull() {
        try {
            LoginUser loginUser = LoginUserHolder.get();
            return loginUser != null ? loginUser.getUserId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
