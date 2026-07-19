package com.campus.application.notice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 未读公告数量
 */
@Data
@Builder
public class NoticeUnreadCountDTO {

    /** 未读总数 */
    private Long total;

    /** 未读学校公告数 */
    private Long school;

    /** 未读院系公告数 */
    private Long department;
}
