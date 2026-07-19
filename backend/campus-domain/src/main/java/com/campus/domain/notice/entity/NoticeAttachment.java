package com.campus.domain.notice.entity;

import lombok.Data;

@Data
public class NoticeAttachment {

    private Long id;
    private Long noticeId;
    private String name;
    private String url;
    private Long size;
}
