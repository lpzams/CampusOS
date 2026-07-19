package com.campus.domain.news.repository;

import com.campus.domain.news.entity.NewsFavorite;

import java.util.List;

public interface NewsFavoriteRepository {

    /**
     * 查用户是否已收藏某新闻
     */
    NewsFavorite findByUserIdAndNewsId(Long userId, Long newsId);

    /**
     * 分页查用户的收藏列表
     */
    List<NewsFavorite> findPageByUserId(Long userId, int pageNum, int pageSize);

    /**
     * 统计用户的收藏总数
     */
    long countByUserId(Long userId);

    /**
     * 新增收藏
     */
    void save(NewsFavorite favorite);

    /**
     * 按用户和新闻删除收藏
     */
    void deleteByUserIdAndNewsId(Long userId, Long newsId);
}
