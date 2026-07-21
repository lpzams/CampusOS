package com.campus.application.notice.service;

import com.campus.application.notice.command.PublishNoticeCommand;
import com.campus.application.notice.command.TopNoticeCommand;
import com.campus.application.notice.command.UpdateNoticeCommand;
import com.campus.application.notice.dto.NoticePublishResponseDTO;
import com.campus.application.notice.dto.NoticeUpdateResponseDTO;
import com.campus.application.notice.dto.TopNoticeResponseDTO;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.notice.entity.Notice;
import com.campus.domain.notice.entity.NoticeAttachment;
import com.campus.domain.notice.repository.NoticeAttachmentRepository;
import com.campus.domain.notice.repository.NoticeRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeAdminService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 4.5 发布公告 ====================

    @Transactional(rollbackFor = Exception.class)
    public NoticePublishResponseDTO publishNotice(PublishNoticeCommand command) {
        // 1. 校验管理员权限
        User currentUser = getAdminUser();

        // 2. 校验类型合法
        validateType(command.getType());

        // 3. 创建公告实体
        Notice notice = Notice.create(
                command.getTitle(),
                command.getContent(),
                command.getType(),
                command.getDepartment(),
                command.getSummary(),
                command.getIsTop() != null && command.getIsTop() ? 1 : 0,
                command.getDeadline() != null ? parseDeadline(command.getDeadline()) : null
        );

        // 4. 保存公告
        noticeRepository.save(notice);

        // 5. 保存附件
        saveAttachments(notice.getId(), command.getAttachments());

        log.info("公告发布成功: id={}, title={}, author={}", notice.getId(), notice.getTitle(), currentUser.getRealName());

        return NoticePublishResponseDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .type(notice.getType())
                .typeDesc(notice.getTypeDesc())
                .createTime(notice.getCreateTime() != null ? notice.getCreateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 4.6 更新公告 ====================

    @Transactional(rollbackFor = Exception.class)
    public NoticeUpdateResponseDTO updateNotice(Long id, UpdateNoticeCommand command) {
        // 1. 校验管理员权限
        getAdminUser();

        // 2. 查公告
        Notice notice = noticeRepository.findById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        }

        // 3. 校验类型合法（如果传了type）
        if (command.getType() != null) {
            validateType(command.getType());
        }

        // 4. 更新字段
        notice.updateFields(
                command.getTitle(),
                command.getContent(),
                command.getType(),
                command.getDepartment(),
                command.getIsTop() != null ? (command.getIsTop() ? 1 : 0) : null,
                command.getDeadline() != null ? parseDeadline(command.getDeadline()) : null
        );
        noticeRepository.update(notice);

        // 5. 如果传了附件列表，全量替换
        if (command.getAttachments() != null) {
            attachmentRepository.deleteByNoticeId(id);
            saveAttachments(id, command.getAttachments());
        }

        log.info("公告更新成功: id={}, title={}", notice.getId(), notice.getTitle());

        return NoticeUpdateResponseDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .updateTime(notice.getUpdateTime() != null ? notice.getUpdateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 4.7 删除公告 ====================

    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        // 1. 校验管理员权限
        getAdminUser();

        // 2. 校验公告存在
        Notice notice = noticeRepository.findById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        }

        // 3. 删除附件
        attachmentRepository.deleteByNoticeId(id);

        // 4. 逻辑删除公告
        noticeRepository.delete(id);

        log.info("公告删除成功: id={}", id);
    }

    // ==================== 4.8 置顶/取消置顶 ====================

    @Transactional(rollbackFor = Exception.class)
    public TopNoticeResponseDTO toggleTop(Long id, TopNoticeCommand command) {
        // 1. 校验管理员权限
        getAdminUser();

        // 2. 查公告
        Notice notice = noticeRepository.findById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        }

        // 3. 更新置顶状态
        notice.toggleTop(command.getIsTop());
        noticeRepository.update(notice);

        log.info("公告置顶状态更新: id={}, isTop={}", id, command.getIsTop());

        return TopNoticeResponseDTO.builder()
                .id(notice.getId())
                .isTop(command.getIsTop())
                .build();
    }

    // ==================== 私有方法 ====================

    private User getAdminUser() {
        Long userId = UserAppService.getCurrentUserId();
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!user.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        return user;
    }

    private void validateType(String type) {
        if (!Notice.TYPE_SCHOOL.equals(type) && !Notice.TYPE_DEPT.equals(type)) {
            throw new BusinessException("公告类型不合法，仅支持 SCHOOL 或 DEPT");
        }
    }

    private void saveAttachments(Long noticeId, List<PublishNoticeCommand.AttachmentItem> attachmentItems) {
        if (attachmentItems == null || attachmentItems.isEmpty()) return;
        List<NoticeAttachment> attachments = new ArrayList<>();
        for (PublishNoticeCommand.AttachmentItem item : attachmentItems) {
            NoticeAttachment att = new NoticeAttachment();
            att.setNoticeId(noticeId);
            att.setName(item.getName());
            att.setUrl(item.getUrl());
            att.setSize(item.getSize() != null ? item.getSize() : 0L);
            attachments.add(att);
        }
        attachmentRepository.saveBatch(noticeId, attachments);
    }

    private LocalDateTime parseDeadline(String deadline) {
        try {
            return LocalDateTime.parse(deadline, FORMATTER);
        } catch (Exception e) {
            throw new BusinessException("截止时间格式不正确，正确格式：yyyy-MM-dd HH:mm:ss");
        }
    }
}
