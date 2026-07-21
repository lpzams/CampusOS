package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 置顶操作响应
 */
@Data
@Builder
public class TopNoticeResponseDTO {

    private Long id;
    private Boolean isTop;
}
