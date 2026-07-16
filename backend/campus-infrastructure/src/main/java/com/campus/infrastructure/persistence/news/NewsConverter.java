package com.campus.infrastructure.persistence.news;

import com.campus.domain.news.News;

/**
 * 新闻 PO ↔ 领域实体 转换器。
 *
 * <p>【DDD 洋葱架构·infrastructure 层】
 * <ul>
 *   <li>领域实体 {@link News} 和数据库 {@link NewsPO} 是两个类，
 *       它们之间的搬运工作全部收敛在这里，其它地方不允许直接 new 对方。</li>
 *   <li>这样表结构或领域模型任意一方改动，只需要改这一个文件。</li>
 * </ul>
 *
 * <p>状态枚举：领域层用 {@link News.Status} 枚举，数据库存同名字符串，
 * 这里做 name() ↔ valueOf() 的对应，容错为 null。
 *
 * <p>【新增功能时】照抄本类：在 persistence.你的功能 下建一个 XxxConverter，
 * 提供 toDomain / toPO 两个静态方法即可。
 */
public final class NewsConverter {

    private NewsConverter() {
    }

    /** PO → 领域实体（从数据库读出来后，交给上层用） */
    public static News toDomain(NewsPO po) {
        if (po == null) {
            return null;
        }
        News news = new News();
        news.setId(po.getId());
        news.setTitle(po.getTitle());
        news.setContent(po.getContent());
        news.setCategory(po.getCategory());
        news.setCoverImage(po.getCoverImage());
        news.setAuthor(po.getAuthor());
        news.setStatus(parseStatus(po.getStatus()));
        news.setViewCount(po.getViewCount());
        news.setPublishTime(po.getPublishTime());
        news.setCreateTime(po.getCreateTime());
        news.setUpdateTime(po.getUpdateTime());
        return news;
    }

    /** 领域实体 → PO（准备写库时用） */
    public static NewsPO toPO(News news) {
        if (news == null) {
            return null;
        }
        NewsPO po = new NewsPO();
        po.setId(news.getId());
        po.setTitle(news.getTitle());
        po.setContent(news.getContent());
        po.setCategory(news.getCategory());
        po.setCoverImage(news.getCoverImage());
        po.setAuthor(news.getAuthor());
        po.setStatus(news.getStatus() == null ? null : news.getStatus().name());
        po.setViewCount(news.getViewCount());
        po.setPublishTime(news.getPublishTime());
        po.setCreateTime(news.getCreateTime());
        po.setUpdateTime(news.getUpdateTime());
        return po;
    }

    private static News.Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return News.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
