package com.campus.api.controller;

import com.campus.application.notice.command.NoticeListQuery;
import com.campus.application.notice.dto.*;
import com.campus.application.notice.service.NoticeAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Tag(name = "校园公告", description = "公告列表、详情、已读标记相关接口")
public class NoticeController {

    private final NoticeAppService noticeAppService;

    // ==================== 4.1 获取公告列表 ====================

    @GetMapping("/list")
    @Operation(summary = "获取公告列表")
    public Result<PageResult<NoticeListDTO>> getNoticeList(NoticeListQuery query) {
        log.info("获取公告列表: type={}, department={}, page={}, size={}",
                query.getType(), query.getDepartment(), query.getPageNum(), query.getPageSize());
        return Result.success(noticeAppService.getNoticeList(query));
    }

    // ==================== 4.2 获取公告详情 ====================

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取公告详情")
    public Result<NoticeDetailDTO> getNoticeDetail(@PathVariable Long id) {
        log.info("获取公告详情: id={}", id);
        return Result.success(noticeAppService.getNoticeDetail(id));
    }

    // ==================== 4.3 标记公告已读 ====================

    @PostMapping("/read/{id}")
    @Operation(summary = "标记公告已读")
    public Result<NoticeReadResponseDTO> markAsRead(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("标记公告已读: userId={}, noticeId={}", userId, id);
        return Result.success(noticeAppService.markAsRead(userId, id));
    }

    // ==================== 4.4 获取未读公告数量 ====================

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读公告数量")
    public Result<NoticeUnreadCountDTO> getUnreadCount() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取未读公告数量: userId={}", userId);
        return Result.success(noticeAppService.getUnreadCount(userId));
    }
}
