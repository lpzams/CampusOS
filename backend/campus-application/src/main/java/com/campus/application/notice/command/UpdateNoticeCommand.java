package com.campus.application.notice.command;

import lombok.Data;

import java.util.List;

/**
 * 更新公告命令（管理员，所有字段可选）
 */
@Data
public class UpdateNoticeCommand {

    private String title;
    private String content;
    private String type;
    private String department;
    private String summary;
    private Boolean isTop;
    private String deadline;

    /** 附件列表（传则全量替换） */
    private List<PublishNoticeCommand.AttachmentItem> attachments;
}
