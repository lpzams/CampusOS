package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NewsCategoryMapper extends BaseMapper<NewsCategoryPO> {

    /**
     * 查所有分类，包含各分类的已发布新闻数量
     */
    @Select("SELECT c.*, " +
            "(SELECT COUNT(*) FROM t_news n WHERE n.category_id = c.id AND n.deleted = 0 AND n.is_published = 1) AS count " +
            "FROM t_news_category c " +
            "WHERE c.deleted = 0 " +
            "ORDER BY c.sort_order ASC")
    List<NewsCategoryPO> selectAllWithCount();

    /**
     * 判断分类是否存在
     */
    @Select("SELECT COUNT(*) FROM t_news_category WHERE id = #{id} AND deleted = 0")
    int countById(@Param("id") Long id);
}
