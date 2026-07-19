package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_news_category")
public class NewsCategoryPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    private Integer deleted;

    /** 该分类下的新闻数量（非数据库字段，查询时计算） */
    @TableField(exist = false)
    private Integer count;
}
