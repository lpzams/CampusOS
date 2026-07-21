package com.campus.domain.news.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsCategory {

    private Long id;
    private String name;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /** 该分类下的新闻数量（非数据库字段，查询时计算） */
    private Integer count;
}
