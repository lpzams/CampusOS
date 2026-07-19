package com.campus.infrastructure.persistence.news;

import com.campus.domain.news.entity.News;
import com.campus.domain.news.entity.NewsCategory;
import com.campus.domain.news.entity.NewsFavorite;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsConverter {

    // ===== News 转换 =====

    public News toNews(NewsPO po) {
        if (po == null) return null;
        News news = new News();
        BeanUtils.copyProperties(po, news);
        return news;
    }

    public NewsPO toNewsPO(News news) {
        if (news == null) return null;
        NewsPO po = new NewsPO();
        BeanUtils.copyProperties(news, po);
        return po;
    }

    public List<News> toNewsList(List<NewsPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return new ArrayList<>();
        }
        return poList.stream()
                .map(this::toNews)
                .collect(Collectors.toList());
    }

    // ===== NewsCategory 转换 =====

    public NewsCategory toNewsCategory(NewsCategoryPO po) {
        if (po == null) return null;
        NewsCategory category = new NewsCategory();
        BeanUtils.copyProperties(po, category);
        return category;
    }

    public List<NewsCategory> toNewsCategoryList(List<NewsCategoryPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return new ArrayList<>();
        }
        return poList.stream()
                .map(this::toNewsCategory)
                .collect(Collectors.toList());
    }

    // ===== NewsFavorite 转换 =====

    public NewsFavorite toNewsFavorite(NewsFavoritePO po) {
        if (po == null) return null;
        NewsFavorite favorite = new NewsFavorite();
        BeanUtils.copyProperties(po, favorite);
        return favorite;
    }

    public NewsFavoritePO toNewsFavoritePO(NewsFavorite favorite) {
        if (favorite == null) return null;
        NewsFavoritePO po = new NewsFavoritePO();
        BeanUtils.copyProperties(favorite, po);
        return po;
    }

    public List<NewsFavorite> toNewsFavoriteList(List<NewsFavoritePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return new ArrayList<>();
        }
        return poList.stream()
                .map(this::toNewsFavorite)
                .collect(Collectors.toList());
    }
}
