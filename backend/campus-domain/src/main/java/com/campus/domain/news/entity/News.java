package com.campus.domain.news.entity;

import com.campus.common.exception.BusinessException;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class News {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private Long categoryId;
    private String author;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer isTop;
    private Integer isPublished;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 工厂方法 ==========

    /**
     * 创建新闻（管理员发布）
     */
    public static News create(String title, String content, String coverImage,
                              Long categoryId, String summary, Integer isTop, Integer isPublished, String author) {
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setCoverImage(coverImage);
        news.setCategoryId(categoryId);
        news.setSummary(summary);
        news.setIsTop(isTop != null ? isTop : 0);
        news.setIsPublished(isPublished != null ? isPublished : 0);
        news.setAuthor(author);
        news.setViewCount(0);
        news.setFavoriteCount(0);
        news.setCreateTime(LocalDateTime.now());
        news.setUpdateTime(LocalDateTime.now());
        return news;
    }

    // ========== 业务方法 ==========

    /**
     * 发布新闻
     */
    public void publish() {
        if (this.isPublished != null && this.isPublished == 1) {
            throw new BusinessException("新闻已发布，无需重复发布");
        }
        this.isPublished = 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 下架新闻
     */
    public void unpublish() {
        if (this.isPublished == null || this.isPublished == 0) {
            throw new BusinessException("新闻未发布，无法下架");
        }
        this.isPublished = 0;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 浏览数 +1（仅在 Repository 层通过 SQL 原子操作，此处为 Entity 层面预留）
     */
    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    /**
     * 收藏数 +1
     */
    public void incrementFavoriteCount() {
        if (this.favoriteCount == null) {
            this.favoriteCount = 0;
        }
        this.favoriteCount++;
    }

    /**
     * 收藏数 -1
     */
    public void decrementFavoriteCount() {
        if (this.favoriteCount == null || this.favoriteCount <= 0) {
            this.favoriteCount = 0;
            return;
        }
        this.favoriteCount--;
    }

    /**
     * 校验新闻是否存在
     */
    public void checkExists() {
        if (this.id == null) {
            throw new BusinessException("新闻不存在");
        }
    }
}
