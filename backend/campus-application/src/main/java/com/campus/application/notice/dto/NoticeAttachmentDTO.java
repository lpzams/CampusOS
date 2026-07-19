package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 附件项
 */
@Data
@Builder
public class NoticeAttachmentDTO {

    private Long id;
    private String name;
    private String url;
    private Long size;
}
