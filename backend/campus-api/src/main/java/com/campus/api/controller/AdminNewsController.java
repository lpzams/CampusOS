package com.campus.api.controller;

import com.campus.application.news.command.PublishNewsCommand;
import com.campus.application.news.dto.NewsDetailDTO;
import com.campus.application.news.service.NewsAdminService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
@Tag(name = "新闻管理", description = "管理员新闻发布相关接口")
public class AdminNewsController {

    private final NewsAdminService newsAdminService;

    // ==================== 3.7 后台发布新闻（管理员） ====================

    @PostMapping
    @Operation(summary = "发布新闻（管理员）")
    public Result<NewsDetailDTO> publishNews(@Valid @RequestBody PublishNewsCommand command) {
        log.info("后台发布新闻: title={}, categoryId={}", command.getTitle(), command.getCategoryId());
        return Result.success(newsAdminService.publishNews(command));
    }
}
