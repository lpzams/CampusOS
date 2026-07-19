package com.campus.application.news.service;

import com.campus.application.news.command.NewsListQuery;
import com.campus.application.news.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.context.LoginUser;
import com.campus.common.context.LoginUserHolder;
import com.campus.common.exception.BusinessException;
import com.campus.domain.news.entity.News;
import com.campus.domain.news.entity.NewsCategory;
import com.campus.domain.news.entity.NewsFavorite;
import com.campus.domain.news.repository.NewsCategoryRepository;
import com.campus.domain.news.repository.NewsFavoriteRepository;
import com.campus.domain.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAppService {

    private final NewsRepository newsRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final NewsFavoriteRepository newsFavoriteRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 3.1 获取新闻列表 ====================

    public PageResult<NewsListDTO> getNewsList(NewsListQuery query) {
        // 1. 查分类列表（用于 categoryId → category name 映射）
        List<NewsCategory> categories = newsCategoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(NewsCategory::getId, NewsCategory::getName));

        // 2. 分页查新闻
        List<News> newsList = newsRepository.findPublishedPage(
                query, query.getCategoryId(), query.getKeyword());
        long total = newsRepository.countPublished(query.getCategoryId(), query.getKeyword());

        // 3. 拼装 DTO
        List<NewsListDTO> dtoList = newsList.stream()
                .map(news -> NewsListDTO.builder()
                        .id(news.getId())
                        .title(news.getTitle())
                        .summary(news.getSummary())
                        .coverImage(news.getCoverImage())
                        .category(categoryMap.getOrDefault(news.getCategoryId(), "未知"))
                        .categoryId(news.getCategoryId())
                        .viewCount(news.getViewCount())
                        .isTop(news.getIsTop() != null && news.getIsTop() == 1)
                        .createTime(news.getCreateTime() != null ? news.getCreateTime().format(FORMATTER) : null)
                        .build())
                .collect(Collectors.toList());

        return PageResult.of(total, dtoList, query.getPageNum(), query.getPageSize());
    }

    // ==================== 3.2 获取新闻详情 ====================

    @Transactional(rollbackFor = Exception.class)
    public NewsDetailDTO getNewsDetail(Long newsId) {
        // 1. 查新闻
        News news = newsRepository.findById(newsId);
        if (news == null) {
            throw new BusinessException(ResultCode.NEWS_NOT_FOUND);
        }

        // 2. 浏览数 +1
        newsRepository.incrementViewCount(newsId);
        // 手动更新内存中的 viewCount 以返回正确值
        news.setViewCount(news.getViewCount() == null ? 1 : news.getViewCount() + 1);

        // 3. 查分类名称
        NewsCategory category = newsCategoryRepository.findById(news.getCategoryId());

        // 4. 判断当前用户是否已收藏
        boolean isFavorite = false;
        try {
            Long userId = getCurrentUserIdOrNull();
            if (userId != null) {
                NewsFavorite favorite = newsFavoriteRepository.findByUserIdAndNewsId(userId, newsId);
                isFavorite = (favorite != null);
            }
        } catch (Exception e) {
            // 未登录用户忽略
        }

        // 5. 拼装 DTO
        return NewsDetailDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .coverImage(news.getCoverImage())
                .category(category != null ? category.getName() : "未知")
                .categoryId(news.getCategoryId())
                .author(news.getAuthor())
                .viewCount(news.getViewCount())
                .favoriteCount(news.getFavoriteCount())
                .isFavorite(isFavorite)
                .isTop(news.getIsTop() != null && news.getIsTop() == 1)
                .createTime(news.getCreateTime() != null ? news.getCreateTime().format(FORMATTER) : null)
                .updateTime(news.getUpdateTime() != null ? news.getUpdateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 3.3 获取新闻分类 ====================

    public List<NewsCategoryDTO> getCategories() {
        List<NewsCategory> categories = newsCategoryRepository.findAll();
        return categories.stream()
                .map(cat -> NewsCategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .description(cat.getDescription())
                        .count(cat.getCount())
                        .sortOrder(cat.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== 3.4 收藏新闻 ====================

    @Transactional(rollbackFor = Exception.class)
    public FavoriteResponseDTO favoriteNews(Long userId, Long newsId) {
        // 1. 校验新闻存在
        News news = newsRepository.findById(newsId);
        if (news == null) {
            throw new BusinessException(ResultCode.NEWS_NOT_FOUND);
        }

        // 2. 校验是否已收藏（幂等处理）
        NewsFavorite existing = newsFavoriteRepository.findByUserIdAndNewsId(userId, newsId);
        if (existing != null) {
            throw new BusinessException(ResultCode.NEWS_ALREADY_FAVORITED);
        }

        // 3. 创建收藏
        NewsFavorite favorite = NewsFavorite.create(userId, newsId);
        newsFavoriteRepository.save(favorite);

        // 4. 新闻收藏数 +1
        newsRepository.incrementFavoriteCount(newsId);

        log.info("收藏成功: userId={}, newsId={}, favoriteId={}", userId, newsId, favorite.getId());

        return FavoriteResponseDTO.builder()
                .favoriteId(favorite.getId())
                .newsId(newsId)
                .favoriteTime(favorite.getCreateTime() != null ? favorite.getCreateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 3.5 取消收藏 ====================

    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteNews(Long userId, Long newsId) {
        // 1. 校验收藏记录是否存在
        NewsFavorite existing = newsFavoriteRepository.findByUserIdAndNewsId(userId, newsId);
        if (existing == null) {
            throw new BusinessException(ResultCode.NEWS_NOT_FAVORITED);
        }

        // 2. 删除收藏
        newsFavoriteRepository.deleteByUserIdAndNewsId(userId, newsId);

        // 3. 新闻收藏数 -1
        newsRepository.decrementFavoriteCount(newsId);

        log.info("取消收藏成功: userId={}, newsId={}", userId, newsId);
    }

    // ==================== 3.6 获取收藏列表 ====================

    public PageResult<NewsFavoriteDTO> getMyFavorites(Long userId, int page, int size) {
        // 1. 查分类映射
        List<NewsCategory> categories = newsCategoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(NewsCategory::getId, NewsCategory::getName));

        // 2. 分页查收藏记录
        List<NewsFavorite> favorites = newsFavoriteRepository.findPageByUserId(userId, page, size);
        long total = newsFavoriteRepository.countByUserId(userId);

        // 3. 根据 newsId 批量查询新闻信息
        List<Long> newsIds = favorites.stream()
                .map(NewsFavorite::getNewsId)
                .collect(Collectors.toList());

        // 4. 拼装 DTO（逐条查新闻，收藏列表通常不太大）
        List<NewsFavoriteDTO> dtoList = favorites.stream()
                .map(fav -> {
                    News news = newsRepository.findById(fav.getNewsId());
                    if (news == null) return null;
                    return NewsFavoriteDTO.builder()
                            .id(news.getId())
                            .title(news.getTitle())
                            .summary(news.getSummary())
                            .coverImage(news.getCoverImage())
                            .category(categoryMap.getOrDefault(news.getCategoryId(), "未知"))
                            .categoryId(news.getCategoryId())
                            .viewCount(news.getViewCount())
                            .favoriteTime(fav.getCreateTime() != null ? fav.getCreateTime().format(FORMATTER) : null)
                            .createTime(news.getCreateTime() != null ? news.getCreateTime().format(FORMATTER) : null)
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return PageResult.of(total, dtoList, page, size);
    }

    // ==================== 私有方法 ====================

    /**
     * 尝试获取当前登录用户ID（未登录时返回 null）
     */
    private Long getCurrentUserIdOrNull() {
        try {
            LoginUser loginUser = LoginUserHolder.get();
            return loginUser != null ? loginUser.getUserId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
