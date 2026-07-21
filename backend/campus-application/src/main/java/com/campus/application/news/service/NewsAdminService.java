package com.campus.application.news.service;

import com.campus.application.news.command.PublishNewsCommand;
import com.campus.application.news.dto.NewsDetailDTO;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.ResultCode;
import com.campus.common.constant.UserConstants;
import com.campus.common.exception.BusinessException;
import com.campus.domain.news.entity.News;
import com.campus.domain.news.entity.NewsCategory;
import com.campus.domain.news.repository.NewsCategoryRepository;
import com.campus.domain.news.repository.NewsRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAdminService {

    private final NewsRepository newsRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 3.7 后台发布新闻（管理员） ====================

    @Transactional(rollbackFor = Exception.class)
    public NewsDetailDTO publishNews(PublishNewsCommand command) {
        // 1. 校验管理员权限
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 2. 校验分类存在
        if (!newsCategoryRepository.existsById(command.getCategoryId())) {
            throw new BusinessException(ResultCode.NEWS_CATEGORY_NOT_FOUND);
        }

        // 3. 创建新闻
        News news = News.create(
                command.getTitle(),
                command.getContent(),
                command.getCoverImage(),
                command.getCategoryId(),
                command.getSummary(),
                command.getIsTop(),
                command.getIsPublished() != null ? command.getIsPublished() : 1,
                currentUser.getRealName()
        );

        // 4. 保存
        newsRepository.save(news);

        // 5. 查分类名称
        NewsCategory category = newsCategoryRepository.findById(news.getCategoryId());

        log.info("新闻发布成功: newsId={}, title={}, author={}", news.getId(), news.getTitle(), currentUser.getRealName());

        // 6. 拼装响应
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
                .isTop(news.getIsTop() != null && news.getIsTop() == 1)
                .createTime(news.getCreateTime() != null ? news.getCreateTime().format(FORMATTER) : null)
                .updateTime(news.getUpdateTime() != null ? news.getUpdateTime().format(FORMATTER) : null)
                .build();
    }
}
