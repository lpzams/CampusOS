package com.campus.api.controller;

import com.campus.application.notice.command.PublishNoticeCommand;
import com.campus.application.notice.command.TopNoticeCommand;
import com.campus.application.notice.command.UpdateNoticeCommand;
import com.campus.application.notice.dto.NoticePublishResponseDTO;
import com.campus.application.notice.dto.NoticeUpdateResponseDTO;
import com.campus.application.notice.dto.TopNoticeResponseDTO;
import com.campus.application.notice.service.NoticeAdminService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/notice")
@RequiredArgsConstructor
@Tag(name = "公告管理", description = "管理员公告发布、更新、删除相关接口")
public class AdminNoticeController {

    private final NoticeAdminService noticeAdminService;

    // ==================== 4.5 发布公告 ====================

    @PostMapping
    @Operation(summary = "发布公告（管理员）")
    public Result<NoticePublishResponseDTO> publishNotice(@Valid @RequestBody PublishNoticeCommand command) {
        log.info("后台发布公告: title={}, type={}", command.getTitle(), command.getType());
        return Result.success(noticeAdminService.publishNotice(command));
    }

    // ==================== 4.6 更新公告 ====================

    @PutMapping("/{id}")
    @Operation(summary = "更新公告（管理员）")
    public Result<NoticeUpdateResponseDTO> updateNotice(@PathVariable Long id,
                                                         @RequestBody UpdateNoticeCommand command) {
        log.info("后台更新公告: id={}", id);
        return Result.success(noticeAdminService.updateNotice(id, command));
    }

    // ==================== 4.7 删除公告 ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告（管理员）")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        log.info("后台删除公告: id={}", id);
        noticeAdminService.deleteNotice(id);
        return Result.success();
    }

    // ==================== 4.8 置顶/取消置顶 ====================

    @PutMapping("/{id}/top")
    @Operation(summary = "置顶/取消置顶公告（管理员）")
    public Result<TopNoticeResponseDTO> toggleTop(@PathVariable Long id,
                                                    @Valid @RequestBody TopNoticeCommand command) {
        log.info("公告置顶操作: id={}, isTop={}", id, command.getIsTop());
        return Result.success(noticeAdminService.toggleTop(id, command));
    }
}
