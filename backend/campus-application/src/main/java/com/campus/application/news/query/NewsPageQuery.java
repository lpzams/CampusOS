package com.campus.application.news.query;

import com.campus.common.query.PageQuery;

/**
 * 新闻分页查询条件（Query）—— 应用层「读操作」的入参。
 *
 * <p>继承 {@link PageQuery} 拿到 {@code pageNum / pageSize} 两个分页字段，
 * 这里只需补充本功能特有的过滤条件（关键字、栏目）。</p>
 *
 * <p>Controller 用 {@code @ModelAttribute}（即 query string）接收，
 * 例如：{@code GET /api/news?pageNum=1&pageSize=10&keyword=奖学金&category=通知公告}</p>
 */
public class NewsPageQuery extends PageQuery {

    /** 标题关键字，模糊匹配，可空 */
    private String keyword;

    /** 栏目过滤，可空 */
    private String category;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
