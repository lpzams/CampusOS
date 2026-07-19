package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NewsFavoriteMapper extends BaseMapper<NewsFavoritePO> {

    /**
     * 查用户是否已收藏某新闻
     */
    @Select("SELECT * FROM t_news_favorite WHERE user_id = #{userId} AND news_id = #{newsId}")
    NewsFavoritePO selectByUserIdAndNewsId(@Param("userId") Long userId,
                                            @Param("newsId") Long newsId);

    /**
     * 分页查用户的收藏列表（按收藏时间倒序）
     */
    @Select("SELECT * FROM t_news_favorite WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<NewsFavoritePO> selectPageByUserId(@Param("userId") Long userId,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    /**
     * 统计用户收藏总数
     */
    @Select("SELECT COUNT(*) FROM t_news_favorite WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 按用户和新闻删除收藏
     */
    @Delete("DELETE FROM t_news_favorite WHERE user_id = #{userId} AND news_id = #{newsId}")
    int deleteByUserIdAndNewsId(@Param("userId") Long userId,
                                 @Param("newsId") Long newsId);
}
