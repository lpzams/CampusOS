package com.campus.application.news.command;

import com.campus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 新闻列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NewsListQuery extends PageQuery {

    /** 分类ID（可选） */
    private Long categoryId;

    /** 搜索关键词（可选） */
    private String keyword;
}
