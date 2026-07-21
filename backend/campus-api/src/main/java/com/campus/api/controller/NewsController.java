package com.campus.api.controller;

import com.campus.application.news.command.NewsListQuery;
import com.campus.application.news.dto.FavoriteResponseDTO;
import com.campus.application.news.dto.NewsCategoryDTO;
import com.campus.application.news.dto.NewsDetailDTO;
import com.campus.application.news.dto.NewsFavoriteDTO;
import com.campus.application.news.dto.NewsListDTO;
import com.campus.application.news.service.NewsAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "校园新闻", description = "新闻列表、详情、分类、收藏相关接口")
public class NewsController {

    private final NewsAppService newsAppService;

    // ==================== 3.1 获取新闻列表 ====================

    @GetMapping("/list")
    @Operation(summary = "获取新闻列表")
    public Result<PageResult<NewsListDTO>> getNewsList(NewsListQuery query) {
        log.info("获取新闻列表: categoryId={}, keyword={}, page={}, size={}",
                query.getCategoryId(), query.getKeyword(), query.getPageNum(), query.getPageSize());
        return Result.success(newsAppService.getNewsList(query));
    }

    // ==================== 3.2 获取新闻详情 ====================

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取新闻详情")
    public Result<NewsDetailDTO> getNewsDetail(@PathVariable Long id) {
        log.info("获取新闻详情: id={}", id);
        return Result.success(newsAppService.getNewsDetail(id));
    }

    // ==================== 3.3 获取新闻分类 ====================

    @GetMapping("/categories")
    @Operation(summary = "获取新闻分类")
    public Result<List<NewsCategoryDTO>> getCategories() {
        log.info("获取新闻分类");
        return Result.success(newsAppService.getCategories());
    }

    // ==================== 3.4 收藏新闻 ====================

    @PostMapping("/favorite/{id}")
    @Operation(summary = "收藏新闻")
    public Result<FavoriteResponseDTO> favoriteNews(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("收藏新闻: userId={}, newsId={}", userId, id);
        return Result.success(newsAppService.favoriteNews(userId, id));
    }

    // ==================== 3.5 取消收藏 ====================

    @DeleteMapping("/favorite/{id}")
    @Operation(summary = "取消收藏")
    public Result<Void> unfavoriteNews(@PathVariable Long id) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("取消收藏: userId={}, newsId={}", userId, id);
        newsAppService.unfavoriteNews(userId, id);
        return Result.success();
    }

    // ==================== 3.6 获取收藏列表 ====================

    @GetMapping("/favorites")
    @Operation(summary = "获取我的收藏列表")
    public Result<PageResult<NewsFavoriteDTO>> getMyFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取收藏列表: userId={}, page={}, size={}", userId, page, size);
        return Result.success(newsAppService.getMyFavorites(userId, page, size));
    }
}
