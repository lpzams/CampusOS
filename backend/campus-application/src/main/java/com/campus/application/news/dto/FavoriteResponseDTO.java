package com.campus.application.news.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 收藏 / 取消收藏响应
 */
@Data
@Builder
public class FavoriteResponseDTO {

    private Long favoriteId;
    private Long newsId;
    private String favoriteTime;
}
