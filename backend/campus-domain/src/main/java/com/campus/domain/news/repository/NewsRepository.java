package com.campus.domain.news.repository;

import com.campus.domain.news.entity.News;
import com.campus.common.query.PageQuery;

import java.util.List;

public interface NewsRepository {

    /**
     * 分页查询已发布的新闻列表
     *
     * @param query 查询条件（categoryId, keyword, pageNum, pageSize）
     */
    List<News> findPublishedPage(PageQuery query, Long categoryId, String keyword);

    /**
     * 统计已发布新闻数（用于分页 total）
     */
    long countPublished(Long categoryId, String keyword);

    /**
     * 按ID查新闻
     */
    News findById(Long id);

    /**
     * 新增新闻
     */
    void save(News news);

    /**
     * 更新新闻
     */
    void update(News news);

    /**
     * 浏览数 +1（原子操作）
     */
    void incrementViewCount(Long id);

    /**
     * 收藏数 +1（原子操作）
     */
    void incrementFavoriteCount(Long id);

    /**
     * 收藏数 -1（原子操作）
     */
    void decrementFavoriteCount(Long id);
}
