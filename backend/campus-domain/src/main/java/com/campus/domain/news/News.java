package com.campus.domain.news;

import java.time.LocalDateTime;

/**
 * 新闻 —— 领域实体（纯净的业务对象）。
 *
 * <p>【DDD 洋葱架构·最内层】
 * <ul>
 *   <li>不依赖 Spring、MyBatis-Plus 等任何框架，只是普通 Java 对象。</li>
 *   <li>它表达的是"业务里的一条新闻"，而不是"数据库里的一行"。
 *       数据库那一行由 infrastructure 层的 {@code NewsPO} 负责。</li>
 *   <li>业务规则写在这里（如 {@link #publish()}），而不是散落在 Controller/Service。</li>
 * </ul>
 *
 * <p>【新增功能时】照抄本类：在 com.campus.domain.你的功能 下建一个同名实体即可。
 */
public class News {

    /** 新闻状态：草稿 / 已发布 / 已下线 */
    public enum Status {
        DRAFT, PUBLISHED, OFFLINE
    }

    private Long id;
    private String title;
    private String content;
    /** 分类，如 校园新闻 / 学院动态 / 通知公告 / 政策文件 */
    private String category;
    private String coverImage;
    private String author;
    private Status status;
    /** 浏览量 */
    private Integer viewCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 业务行为：发布。把领域规则收敛在实体内部，而不是 Service 里散写 if。 */
    public void publish() {
        if (this.title == null || this.title.isBlank()) {
            throw new IllegalStateException("新闻标题不能为空，无法发布");
        }
        this.status = Status.PUBLISHED;
        this.publishTime = LocalDateTime.now();
    }

    /** 业务行为：下线。 */
    public void offline() {
        this.status = Status.OFFLINE;
    }

    /** 业务行为：增加一次浏览量。 */
    public void increaseView() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    // ===== getter / setter =====

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
