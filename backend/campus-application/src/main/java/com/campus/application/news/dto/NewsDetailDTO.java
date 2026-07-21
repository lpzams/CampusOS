package com.campus.application.news.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 新闻详情
 */
@Data
@Builder
public class NewsDetailDTO {

    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private String category;
    private Long categoryId;
    private String author;
    private Integer viewCount;
    private Integer favoriteCount;
    private Boolean isFavorite;
    private Boolean isTop;
    private String createTime;
    private String updateTime;
}
