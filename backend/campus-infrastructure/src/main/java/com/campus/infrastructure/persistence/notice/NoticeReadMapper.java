package com.campus.infrastructure.persistence.notice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoticeReadMapper extends BaseMapper<NoticeReadPO> {

    @Select("SELECT * FROM t_notice_read WHERE user_id = #{userId} AND notice_id = #{noticeId}")
    NoticeReadPO selectByUserIdAndNoticeId(@Param("userId") Long userId,
                                            @Param("noticeId") Long noticeId);

    @Select("SELECT notice_id FROM t_notice_read WHERE user_id = #{userId}")
    List<Long> selectReadNoticeIdsByUserId(@Param("userId") Long userId);

    /**
     * 统计某类型公告中用户未读的数量
     * = 该类型公告总数 - 该类型公告中用户已读数
     */
    @Select("SELECT COUNT(*) FROM t_notice n " +
            "WHERE n.deleted = 0 AND n.type = #{type} " +
            "AND n.id NOT IN (SELECT notice_id FROM t_notice_read WHERE user_id = #{userId})")
    long countUnreadByType(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 统计所有未读公告数量
     */
    @Select("SELECT COUNT(*) FROM t_notice n " +
            "WHERE n.deleted = 0 " +
            "AND n.id NOT IN (SELECT notice_id FROM t_notice_read WHERE user_id = #{userId})")
    long countUnread(@Param("userId") Long userId);
}
