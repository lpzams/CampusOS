package com.campus.application.news.dto;

import java.time.LocalDateTime;

/**
 * 新闻 DTO —— 应用层对外输出的数据结构。
 *
 * <p>DTO 是「给外层（Controller/前端）看的数据」，与领域实体 {@code News} 解耦：
 * 领域实体可能包含前端不关心的业务字段或方法，而 DTO 只暴露需要展示的字段。
 * 由 {@code NewsAppService} 从领域实体组装得到。</p>
 *
 * <p>新增功能时，本文件对应你功能的「查询返回结构」。</p>
 */
public class NewsDTO {

    /** 新闻 ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 正文内容 */
    private String content;

    /** 作者 / 发布人 */
    private String author;

    /** 所属栏目：校园新闻 / 学院动态 / 通知公告 / 政策文件 */
    private String category;

    /** 浏览量 */
    private Integer viewCount;

    /** 是否已发布 */
    private Boolean published;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
