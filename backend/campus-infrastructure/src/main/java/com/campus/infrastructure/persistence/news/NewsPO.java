package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 新闻 PO（Persistent Object）—— 数据库表 {@code t_news} 的一行。
 *
 * <p>【DDD 洋葱架构·infrastructure 层】
 * <ul>
 *   <li>它是"数据库那一行"，带 MyBatis-Plus 的注解，字段和表列一一对应。</li>
 *   <li>它和领域实体 {@code News} 是两个类，互相转换由 {@link NewsConverter} 负责。
 *       这样即使表结构改了，也不会污染领域层。</li>
 * </ul>
 *
 * <p>【为什么要分开】领域实体表达业务（有 publish() 这种行为），
 * PO 表达存储（有 @TableName、逻辑删除标记这些持久化细节）。二者关注点不同。
 *
 * <p>【新增功能时】照抄本类：在 persistence.你的功能 下建一个 XxxPO，
 * 用 @TableName 指向你的表。
 */
@TableName("t_news")
public class NewsPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    private String category;
    private String coverImage;
    private String author;
    /** 状态：DRAFT / PUBLISHED / OFFLINE，存字符串，和领域枚举同名 */
    private String status;
    private Integer viewCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0 未删除，1 已删除。MyBatis-Plus 自动处理，业务代码无感。 */
    @TableLogic
    private Integer deleted;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
