package com.campus.infrastructure.persistence.notice;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_notice_read")
public class NoticeReadPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("notice_id")
    private Long noticeId;
    @TableField("read_time")
    private LocalDateTime readTime;
}
