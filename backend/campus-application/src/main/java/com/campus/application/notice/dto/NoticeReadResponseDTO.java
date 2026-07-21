package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 已读标记响应
 */
@Data
@Builder
public class NoticeReadResponseDTO {

    private Long noticeId;
    private String readTime;
}
