package com.campus.domain.news;

import java.util.List;
import java.util.Optional;

/**
 * 新闻仓储接口 —— DDD 里的"端口(Port)"。
 *
 * <p>【关键理念】领域层只声明"我需要什么样的存取能力"，但不关心怎么实现。
 * 真正用 MyBatis-Plus 操作数据库的实现类 {@code NewsRepositoryImpl}
 * 在 infrastructure 层。这样领域层就和数据库技术彻底解耦。
 *
 * <p>依赖方向：infrastructure 实现本接口 → 依赖 domain。domain 反过来不认识 infrastructure。
 *
 * <p>【新增功能时】在 com.campus.domain.你的功能 下建一个同名 Repository 接口，
 * 只声明你这个功能需要的方法。
 */
public interface NewsRepository {

    /** 保存（新增或更新），返回带 id 的实体 */
    News save(News news);

    /** 按 id 查询 */
    Optional<News> findById(Long id);

    /** 删除 */
    void deleteById(Long id);

    /**
     * 分页查询已发布的新闻。
     *
     * @param keyword  标题关键字，可为 null
     * @param category 分类，可为 null
     * @param offset   跳过多少条
     * @param limit    取多少条
     */
    List<News> findPublishedPage(String keyword, String category, long offset, long limit);

    /** 统计分页总数，与 findPublishedPage 条件一致 */
    long countPublished(String keyword, String category);
}
