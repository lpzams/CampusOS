package com.campus.application.notice.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 发布公告命令（管理员）
 */
@Data
public class PublishNoticeCommand {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotBlank(message = "公告类型不能为空")
    private String type;

    private String department;

    /** 摘要（不传则从 content 截取） */
    private String summary;

    /** 是否置顶 */
    private Boolean isTop;

    /** 截止时间 */
    private String deadline;

    /** 附件列表 */
    private List<AttachmentItem> attachments;

    @Data
    public static class AttachmentItem {
        private String name;
        private String url;
        private Long size;
    }
}
