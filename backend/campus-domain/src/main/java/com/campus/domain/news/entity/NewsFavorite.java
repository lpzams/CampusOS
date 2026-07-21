package com.campus.domain.news.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsFavorite {

    private Long id;
    private Long userId;
    private Long newsId;
    private LocalDateTime createTime;

    // ========== 工厂方法 ==========

    /**
     * 创建收藏
     */
    public static NewsFavorite create(Long userId, Long newsId) {
        NewsFavorite favorite = new NewsFavorite();
        favorite.setUserId(userId);
        favorite.setNewsId(newsId);
        favorite.setCreateTime(LocalDateTime.now());
        return favorite;
    }
}
