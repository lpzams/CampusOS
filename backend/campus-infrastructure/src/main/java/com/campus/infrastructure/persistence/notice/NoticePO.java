package com.campus.infrastructure.persistence.notice;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_notice")
public class NoticePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type;
    private String department;
    private String summary;
    @TableField("is_top")
    private Integer isTop;
    @TableField("read_count")
    private Integer readCount;
    private LocalDateTime deadline;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
