package com.campus.api.controller.news;

import com.campus.application.news.NewsAppService;
import com.campus.application.news.command.CreateNewsCommand;
import com.campus.application.news.dto.NewsDTO;
import com.campus.application.news.query.NewsPageQuery;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 新闻 Controller —— DDD 洋葱架构·最外层（api）。
 *
 * <p>【职责边界·很重要】
 * <ul>
 *   <li>Controller 只做三件事：接收 HTTP 参数、调用 application 层、包装成统一 {@link Result} 返回。</li>
 *   <li>这里<b>不写任何业务逻辑</b>。业务在 {@link NewsAppService}（编排）和领域实体（规则）里。</li>
 *   <li>返回给前端的永远是 DTO，绝不直接暴露领域实体或 PO。</li>
 * </ul>
 *
 * <p>【新增功能时】照抄本类：在 controller.你的功能 下建一个 XxxController，
 * 注入对应的 XxxAppService，把 URL 前缀改成 /api/你的功能。
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsAppService newsAppService;

    public NewsController(NewsAppService newsAppService) {
        this.newsAppService = newsAppService;
    }

    /** 分页查询已发布新闻（网站首页、小程序首页都用它） */
    @GetMapping
    public Result<PageResult<NewsDTO>> page(NewsPageQuery query) {
        return Result.success(newsAppService.pagePublished(query));
    }

    /** 查看新闻详情（会自动 +1 浏览量） */
    @GetMapping("/{id}")
    public Result<NewsDTO> detail(@PathVariable Long id) {
        return Result.success(newsAppService.getDetail(id));
    }

    /** 发布新闻（后台管理用） */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CreateNewsCommand command) {
        return Result.success(newsAppService.create(command));
    }

    /** 下线新闻（后台管理用） */
    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        newsAppService.offline(id);
        return Result.success();
    }

    /** 删除新闻（后台管理用） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        newsAppService.delete(id);
        return Result.success();
    }
}
