package com.campus.application.notice.command;

import com.campus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeListQuery extends PageQuery {

    /** 公告类型：SCHOOL-学校公告 DEPT-院系公告 */
    private String type;

    /** 院系名称（模糊匹配） */
    private String department;
}
