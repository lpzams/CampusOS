package com.campus.application.news.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 收藏列表项
 */
@Data
@Builder
public class NewsFavoriteDTO {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String category;
    private Long categoryId;
    private Integer viewCount;
    private String favoriteTime;
    private String createTime;
}
