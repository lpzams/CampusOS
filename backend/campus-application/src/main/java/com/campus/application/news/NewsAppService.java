package com.campus.application.news;

import com.campus.application.news.command.CreateNewsCommand;
import com.campus.application.news.dto.NewsDTO;
import com.campus.application.news.query.NewsPageQuery;
import com.campus.common.api.PageResult;
import com.campus.common.exception.BusinessException;
import com.campus.domain.news.News;
import com.campus.domain.news.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 新闻应用服务（Application Service）—— 用例编排层。
 *
 * <p>这是「一个功能对外的入口门面」，Controller 只调它，不直接碰仓储或领域细节。
 * 它的职责：</p>
 * <ol>
 *   <li>接收 Command / Query（外层入参）；</li>
 *   <li>调用领域实体的业务方法 + {@link NewsRepository} 完成用例；</li>
 *   <li>把领域实体组装成 {@link NewsDTO} 返回给外层。</li>
 * </ol>
 *
 * <p>关键：这里<b>只依赖 {@link NewsRepository} 接口</b>（领域层定义的端口），
 * 完全不知道底层是 MyBatis-Plus 还是别的。实现类在 infrastructure 层注入，
 * 这就是洋葱架构「依赖倒置」的落地点。</p>
 *
 * <p>事务边界放在这一层（{@code @Transactional} 加在写方法上）。</p>
 *
 * <p>【新增功能时】照抄本类：在 application.你的功能 下建 XxxAppService，
 * 注入你的 domain 仓储接口，方法名和 Controller 保持一致。</p>
 */
@Service
public class NewsAppService {

    private final NewsRepository newsRepository;

    /** 构造器注入：Spring 会把 infrastructure 层的 NewsRepositoryImpl 注进来 */
    public NewsAppService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * 分页查询已发布的新闻列表（网站首页、小程序首页都用它）。
     */
    public PageResult<NewsDTO> pagePublished(NewsPageQuery query) {
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        long offset = (pageNum - 1) * pageSize;

        long total = newsRepository.countPublished(query.getKeyword(), query.getCategory());
        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }
        List<News> records = newsRepository.findPublishedPage(
                query.getKeyword(), query.getCategory(), offset, pageSize);
        List<NewsDTO> list = records.stream().map(this::toDTO).toList();
        return new PageResult<>(pageNum, pageSize, total, list);
    }

    /**
     * 查询新闻详情，并累加一次浏览量。
     */
    @Transactional
    public NewsDTO getDetail(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("新闻不存在，id=" + id));
        // 业务规则写在领域实体里（充血模型），应用层只负责调用与持久化
        news.increaseView();
        newsRepository.save(news);
        return toDTO(news);
    }

    /**
     * 创建并直接发布一条新闻，返回新闻 id。
     */
    @Transactional
    public Long create(CreateNewsCommand command) {
        News news = new News();
        news.setTitle(command.getTitle());
        news.setContent(command.getContent());
        news.setAuthor(command.getAuthor());
        news.setCategory(command.getCategory());
        news.setViewCount(0);
        // 领域行为：发布（内部校验标题非空并置发布时间）
        news.publish();

        News saved = newsRepository.save(news);
        return saved.getId();
    }

    /**
     * 下线新闻（后台管理用）。
     */
    @Transactional
    public void offline(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("新闻不存在，id=" + id));
        news.offline();
        newsRepository.save(news);
    }

    /**
     * 删除新闻（逻辑删除，由 PO 的 @TableLogic 落地）。
     */
    @Transactional
    public void delete(Long id) {
        newsRepository.deleteById(id);
    }

    // ------------------------------------------------------------
    // 领域实体 -> DTO 的组装。字段多时可抽独立 Assembler，这里内联即可。
    // ------------------------------------------------------------
    private NewsDTO toDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setAuthor(news.getAuthor());
        dto.setCategory(news.getCategory());
        dto.setViewCount(news.getViewCount());
        dto.setPublished(news.getStatus() == News.Status.PUBLISHED);
        dto.setCreatedAt(news.getCreateTime());
        dto.setPublishedAt(news.getPublishTime());
        return dto;
    }
}
