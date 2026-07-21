package com.campus.infrastructure.persistence.news;

import com.campus.domain.news.entity.NewsCategory;
import com.campus.domain.news.repository.NewsCategoryRepository;
import com.campus.infrastructure.cache.NewsCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 新闻分类仓储实现 —— Cache-Aside 模式
 * <p>
 * 分类数据变化少，做 Redis 缓存
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsCategoryRepositoryImpl implements NewsCategoryRepository {

    private final NewsCategoryMapper newsCategoryMapper;
    private final NewsConverter newsConverter;
    private final NewsCacheService newsCacheService;

    @Override
    public List<NewsCategory> findAll() {
        // 1. 查缓存
        List<NewsCategory> cached = newsCacheService.getCategoryList();
        if (cached != null && !cached.isEmpty()) return cached;

        // 2. 查 DB
        List<NewsCategory> categories = newsConverter.toNewsCategoryList(
                newsCategoryMapper.selectAllWithCount());

        // 3. 回填缓存
        if (categories != null) {
            newsCacheService.putCategoryList(categories);
        }
        return categories != null ? categories : Collections.emptyList();
    }

    @Override
    public NewsCategory findById(Long id) {
        if (id == null) return null;
        // 分类数量少，从全量缓存中查找
        List<NewsCategory> all = findAll();
        return all.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        return findById(id) != null;
    }
}
