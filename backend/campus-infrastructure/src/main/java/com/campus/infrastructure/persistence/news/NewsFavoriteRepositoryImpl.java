package com.campus.infrastructure.persistence.news;

import com.campus.domain.news.entity.NewsFavorite;
import com.campus.domain.news.repository.NewsFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 新闻收藏仓储实现 —— 不缓存（收藏属于用户维度数据，变化频繁）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsFavoriteRepositoryImpl implements NewsFavoriteRepository {

    private final NewsFavoriteMapper newsFavoriteMapper;
    private final NewsConverter newsConverter;

    @Override
    public NewsFavorite findByUserIdAndNewsId(Long userId, Long newsId) {
        if (userId == null || newsId == null) return null;
        return newsConverter.toNewsFavorite(
                newsFavoriteMapper.selectByUserIdAndNewsId(userId, newsId));
    }

    @Override
    public List<NewsFavorite> findPageByUserId(Long userId, int pageNum, int pageSize) {
        if (userId == null) return Collections.emptyList();
        int offset = (pageNum - 1) * pageSize;
        return newsConverter.toNewsFavoriteList(
                newsFavoriteMapper.selectPageByUserId(userId, offset, pageSize));
    }

    @Override
    public long countByUserId(Long userId) {
        if (userId == null) return 0;
        return newsFavoriteMapper.countByUserId(userId);
    }

    @Override
    public void save(NewsFavorite favorite) {
        NewsFavoritePO po = newsConverter.toNewsFavoritePO(favorite);
        newsFavoriteMapper.insert(po);
        favorite.setId(po.getId());
    }

    @Override
    public void deleteByUserIdAndNewsId(Long userId, Long newsId) {
        newsFavoriteMapper.deleteByUserIdAndNewsId(userId, newsId);
    }
}
