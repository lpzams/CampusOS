package com.campus.infrastructure.persistence.notice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoticeAttachmentMapper extends BaseMapper<NoticeAttachmentPO> {

    @Select("SELECT * FROM t_notice_attachment WHERE notice_id = #{noticeId}")
    List<NoticeAttachmentPO> selectByNoticeId(@Param("noticeId") Long noticeId);

    @Delete("DELETE FROM t_notice_attachment WHERE notice_id = #{noticeId}")
    int deleteByNoticeId(@Param("noticeId") Long noticeId);
}
