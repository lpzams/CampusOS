package com.campus.domain.news.repository;

import com.campus.domain.news.entity.NewsCategory;

import java.util.List;

public interface NewsCategoryRepository {

    /**
     * 查所有分类（含各分类新闻数统计）
     */
    List<NewsCategory> findAll();

    /**
     * 按ID查分类
     */
    NewsCategory findById(Long id);

    /**
     * 判断分类是否存在
     */
    boolean existsById(Long id);
}
