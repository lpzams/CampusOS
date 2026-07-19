package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_news")
public class NewsPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    @TableField("cover_image")
    private String coverImage;
    @TableField("category_id")
    private Long categoryId;
    private String author;
    @TableField("view_count")
    private Integer viewCount;
    @TableField("favorite_count")
    private Integer favoriteCount;
    @TableField("is_top")
    private Integer isTop;
    @TableField("is_published")
    private Integer isPublished;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
