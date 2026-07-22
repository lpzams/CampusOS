package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.domain.news.News;
import com.campus.domain.news.NewsRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 新闻仓储实现 —— 用 MyBatis-Plus 落地 {@link NewsRepository} 端口。
 *
 * <p>【DDD 洋葱架构·infrastructure 层】
 * <ul>
 *   <li>这是"适配器(Adapter)"：把领域层声明的存取能力，翻译成具体的 MyBatis-Plus 调用。</li>
 *   <li>对外只暴露领域实体 {@link News}，PO 是内部细节，进出这里都经 {@link NewsConverter} 转换。</li>
 *   <li>被 Spring 扫描为 Bean（@Repository），application 层通过 NewsRepository 接口注入它，
 *       不知道也不关心背后是 MyBatis-Plus。</li>
 * </ul>
 *
 * <p>【新增功能时】照抄本类：在 persistence.你的功能 下建 XxxRepositoryImpl，
 * implements 你的 domain 仓储接口，注入对应 Mapper。
 */
@Repository
public class NewsRepositoryImpl implements NewsRepository {

    private final NewsMapper newsMapper;

    public NewsRepositoryImpl(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    @Override
    public News save(News news) {
        NewsPO po = NewsConverter.toPO(news);
        if (po.getId() == null) {
            newsMapper.insert(po);
        } else {
            newsMapper.updateById(po);
        }
        // 回填自增主键
        news.setId(po.getId());
        return news;
    }

    @Override
    public Optional<News> findById(Long id) {
        NewsPO po = newsMapper.selectById(id);
        return Optional.ofNullable(NewsConverter.toDomain(po));
    }

    @Override
    public void deleteById(Long id) {
        // 逻辑删除：因为 NewsPO.deleted 标了 @TableLogic，这里实际执行的是 UPDATE ... SET deleted=1
        newsMapper.deleteById(id);
    }

    @Override
    public List<News> findPublishedPage(String keyword, String category, long offset, long limit) {
        List<NewsPO> records = newsMapper.selectList(buildPublishedWrapper(keyword, category)
                .orderByDesc(NewsPO::getPublishTime)
                .last("LIMIT " + offset + ", " + limit));
        return records.stream()
                .map(NewsConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countPublished(String keyword, String category) {
        return newsMapper.selectCount(buildPublishedWrapper(keyword, category));
    }

    /** 抽出公共查询条件：只查已发布，标题模糊、分类精确，两个条件都可为空 */
    private LambdaQueryWrapper<NewsPO> buildPublishedWrapper(String keyword, String category) {
        LambdaQueryWrapper<NewsPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsPO::getStatus, News.Status.PUBLISHED.name());
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(NewsPO::getTitle, keyword);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(NewsPO::getCategory, category);
        }
        return wrapper;
    }
}
