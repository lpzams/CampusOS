package com.campus.infrastructure.persistence.news;

import com.campus.common.query.PageQuery;
import com.campus.domain.news.entity.News;
import com.campus.domain.news.repository.NewsRepository;
import com.campus.infrastructure.cache.NewsCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 新闻仓储实现 —— Cache-Aside 模式（详情查询走缓存，列表查询直接查DB）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsRepositoryImpl implements NewsRepository {

    private final NewsMapper newsMapper;
    private final NewsConverter newsConverter;
    private final NewsCacheService newsCacheService;

    // ==================== 读操作 ====================

    @Override
    public List<News> findPublishedPage(PageQuery query, Long categoryId, String keyword) {
        // 列表查询变化频繁，不缓存，直接查 DB
        return newsConverter.toNewsList(
                newsMapper.selectPublishedPage(categoryId, keyword, query.getOffset(), query.getPageSize()));
    }

    @Override
    public long countPublished(Long categoryId, String keyword) {
        return newsMapper.countPublished(categoryId, keyword);
    }

    @Override
    public News findById(Long id) {
        if (id == null) return null;

        // 1. 查缓存
        News cached = newsCacheService.getNews(id);
        if (cached != null) return cached;

        // 2. 查 DB
        News news = newsConverter.toNews(newsMapper.selectById(id));

        // 3. 回填缓存
        if (news != null) {
            newsCacheService.putNews(news);
        }
        return news;
    }

    // ==================== 写操作（DB 优先 → 删缓存） ====================

    @Override
    public void save(News news) {
        NewsPO po = newsConverter.toNewsPO(news);
        newsMapper.insert(po);
        news.setId(po.getId());
        // 新增，无旧缓存
    }

    @Override
    public void update(News news) {
        // 1. 先写 DB
        NewsPO po = newsConverter.toNewsPO(news);
        newsMapper.updateById(po);
        // 2. 再删缓存
        newsCacheService.evictNews(news.getId());
    }

    // ==================== 原子计数操作 ====================

    @Override
    public void incrementViewCount(Long id) {
        newsMapper.incrementViewCount(id);
        // 更新后缓存失效，下次读取回填
        newsCacheService.evictNews(id);
    }

    @Override
    public void incrementFavoriteCount(Long id) {
        newsMapper.incrementFavoriteCount(id);
        newsCacheService.evictNews(id);
    }

    @Override
    public void decrementFavoriteCount(Long id) {
        newsMapper.decrementFavoriteCount(id);
        newsCacheService.evictNews(id);
    }
}
