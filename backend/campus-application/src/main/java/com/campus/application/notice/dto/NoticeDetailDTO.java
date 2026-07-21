package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 公告详情
 */
@Data
@Builder
public class NoticeDetailDTO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private String typeDesc;
    private String department;
    private Boolean isTop;
    private Boolean isRead;
    private Integer readCount;
    private String createTime;
    private String updateTime;
    private String deadline;
    private List<NoticeAttachmentDTO> attachments;
}
