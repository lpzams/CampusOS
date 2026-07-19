package com.campus.application.news.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 新闻列表项
 */
@Data
@Builder
public class NewsListDTO {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String category;
    private Long categoryId;
    private Integer viewCount;
    private Boolean isTop;
    private String createTime;
}
