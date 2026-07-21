package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 更新公告响应
 */
@Data
@Builder
public class NoticeUpdateResponseDTO {

    private Long id;
    private String title;
    private String updateTime;
}
