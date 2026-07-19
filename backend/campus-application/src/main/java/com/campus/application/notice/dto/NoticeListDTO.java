package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 公告列表项
 */
@Data
@Builder
public class NoticeListDTO {

    private Long id;
    private String title;
    private String type;
    private String typeDesc;
    private String department;
    private String summary;
    private Boolean isTop;
    private Boolean isRead;
    private String createTime;
    private String deadline;
}
