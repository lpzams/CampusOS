package com.campus.application.news.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 新闻分类
 */
@Data
@Builder
public class NewsCategoryDTO {

    private Long id;
    private String name;
    private String description;
    private Integer count;
    private Integer sortOrder;
}
