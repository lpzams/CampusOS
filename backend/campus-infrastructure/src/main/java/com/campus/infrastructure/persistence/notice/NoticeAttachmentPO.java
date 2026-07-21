package com.campus.infrastructure.persistence.notice;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_notice_attachment")
public class NoticeAttachmentPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("notice_id")
    private Long noticeId;
    private String name;
    private String url;
    private Long size;
    @TableField("create_time")
    private LocalDateTime createTime;
}
