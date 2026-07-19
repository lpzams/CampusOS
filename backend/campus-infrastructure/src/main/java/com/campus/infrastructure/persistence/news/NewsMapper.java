package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NewsMapper extends BaseMapper<NewsPO> {

    /**
     * 分页查已发布的新闻（支持分类筛选和关键词搜索）
     */
    @Select("<script>" +
            "SELECT * FROM t_news WHERE deleted = 0 AND is_published = 1 " +
            "<if test='categoryId != null'> AND category_id = #{categoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (title LIKE CONCAT('%',#{keyword},'%') OR summary LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "ORDER BY is_top DESC, create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<NewsPO> selectPublishedPage(@Param("categoryId") Long categoryId,
                                      @Param("keyword") String keyword,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    /**
     * 统计已发布新闻数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_news WHERE deleted = 0 AND is_published = 1 " +
            "<if test='categoryId != null'> AND category_id = #{categoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (title LIKE CONCAT('%',#{keyword},'%') OR summary LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "</script>")
    long countPublished(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword);

    /**
     * 浏览数 +1（原子操作）
     */
    @Update("UPDATE t_news SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 收藏数 +1（原子操作）
     */
    @Update("UPDATE t_news SET favorite_count = favorite_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementFavoriteCount(@Param("id") Long id);

    /**
     * 收藏数 -1（原子操作，确保不为负）
     */
    @Update("UPDATE t_news SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE id = #{id} AND deleted = 0")
    int decrementFavoriteCount(@Param("id") Long id);
}
