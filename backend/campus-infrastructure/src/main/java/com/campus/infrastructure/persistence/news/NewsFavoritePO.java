package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_news_favorite")
public class NewsFavoritePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("news_id")
    private Long newsId;
    @TableField("create_time")
    private LocalDateTime createTime;
}
