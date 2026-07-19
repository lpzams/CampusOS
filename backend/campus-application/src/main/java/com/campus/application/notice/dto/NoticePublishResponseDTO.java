package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 发布公告响应
 */
@Data
@Builder
public class NoticePublishResponseDTO {

    private Long id;
    private String title;
    private String type;
    private String typeDesc;
    private String createTime;
}
