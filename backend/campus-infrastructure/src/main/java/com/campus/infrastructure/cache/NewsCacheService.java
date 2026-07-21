package com.campus.infrastructure.cache;

import com.campus.domain.news.entity.News;
import com.campus.domain.news.entity.NewsCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 新闻缓存服务 —— Cache-Aside 模式
 * <p>
 * 缓存策略：
 * <ul>
 *   <li>新闻详情：Redis 缓存（news:info:{id}），过期 30 分钟</li>
 *   <li>新闻分类：Redis 缓存（news:categories），过期 1 小时</li>
 *   <li>新闻列表：不缓存（变化频繁）</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // ========== Key 前缀 ==========

    private static final String NEWS_INFO_PREFIX = "news:info:";
    private static final String NEWS_CATEGORIES_KEY = "news:categories";

    // ========== 过期时间 ==========

    /** 新闻详情缓存：30分钟 */
    private static final int NEWS_INFO_EXPIRE_SECONDS = 1800;
    /** 分类缓存：1小时 */
    private static final int CATEGORIES_EXPIRE_SECONDS = 3600;

    // ==================== 新闻详情缓存 ====================

    /**
     * 从缓存获取新闻详情
     */
    public News getNews(Long newsId) {
        if (newsId == null) return null;
        String key = NEWS_INFO_PREFIX + newsId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, News.class);
        } catch (Exception e) {
            log.warn("反序列化新闻缓存失败: key={}", key);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 回填新闻详情缓存
     */
    public void putNews(News news) {
        if (news == null || news.getId() == null) return;
        try {
            String json = objectMapper.writeValueAsString(news);
            stringRedisTemplate.opsForValue().set(
                    NEWS_INFO_PREFIX + news.getId(),
                    json,
                    NEWS_INFO_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );
            log.debug("回填新闻缓存: newsId={}", news.getId());
        } catch (JsonProcessingException e) {
            log.error("序列化 News 失败: newsId={}", news.getId(), e);
        }
    }

    /**
     * 删除新闻详情缓存
     */
    public void evictNews(Long newsId) {
        if (newsId == null) return;
        stringRedisTemplate.delete(NEWS_INFO_PREFIX + newsId);
        log.debug("删除新闻缓存: newsId={}", newsId);
    }

    // ==================== 分类列表缓存 ====================

    /**
     * 从缓存获取分类列表
     */
    public List<NewsCategory> getCategoryList() {
        String json = stringRedisTemplate.opsForValue().get(NEWS_CATEGORIES_KEY);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<NewsCategory>>() {});
        } catch (Exception e) {
            log.warn("反序列化分类列表缓存失败");
            stringRedisTemplate.delete(NEWS_CATEGORIES_KEY);
            return null;
        }
    }

    /**
     * 回填分类列表缓存
     */
    public void putCategoryList(List<NewsCategory> categories) {
        if (categories == null || categories.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(categories);
            stringRedisTemplate.opsForValue().set(
                    NEWS_CATEGORIES_KEY,
                    json,
                    CATEGORIES_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );
            log.debug("回填分类列表缓存: count={}", categories.size());
        } catch (JsonProcessingException e) {
            log.error("序列化分类列表失败", e);
        }
    }

    /**
     * 删除分类列表缓存（新增/编辑分类后调用）
     */
    public void evictCategoryList() {
        stringRedisTemplate.delete(NEWS_CATEGORIES_KEY);
        log.debug("删除分类列表缓存");
    }
}
