package com.campus.domain.notice.repository;

import com.campus.domain.notice.entity.Notice;

import java.util.List;

public interface NoticeRepository {

    /** 分页查公告列表（支持类型/部门筛选） */
    List<Notice> findPage(String type, String department, int offset, int size);

    /** 统计公告总数 */
    long count(String type, String department);

    /** 按ID查公告 */
    Notice findById(Long id);

    /** 统计各类型公告总数（用于未读数量计算） */
    long countByType(String type);

    /** 阅读数+1 */
    void incrementReadCount(Long id);

    /** 新增公告 */
    void save(Notice notice);

    /** 更新公告 */
    void update(Notice notice);

    /** 删除公告（逻辑删除） */
    void delete(Long id);
}
