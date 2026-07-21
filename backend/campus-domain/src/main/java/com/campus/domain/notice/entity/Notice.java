package com.campus.domain.notice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notice {

    private Long id;
    private String title;
    private String content;
    /** 公告类型：SCHOOL-学校公告 DEPT-院系公告 */
    private String type;
    private String department;
    private String summary;
    private Integer isTop;
    private Integer readCount;
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========

    public static final String TYPE_SCHOOL = "SCHOOL";
    public static final String TYPE_DEPT = "DEPT";

    // ========== 业务方法 ==========

    public String getTypeDesc() {
        if (TYPE_SCHOOL.equals(type)) return "学校公告";
        if (TYPE_DEPT.equals(type)) return "院系公告";
        return type;
    }

    public boolean isSchool() {
        return TYPE_SCHOOL.equals(type);
    }

    public boolean isDept() {
        return TYPE_DEPT.equals(type);
    }

    // ========== 工厂方法 ==========

    public static Notice create(String title, String content, String type,
                                 String department, String summary,
                                 Integer isTop, LocalDateTime deadline) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type);
        notice.setDepartment(department);
        notice.setSummary(summary);
        notice.setIsTop(isTop != null ? isTop : 0);
        notice.setDeadline(deadline);
        notice.setReadCount(0);
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        return notice;
    }

    /**
     * 更新公告字段
     */
    public void updateFields(String title, String content, String type,
                             String department, Integer isTop, LocalDateTime deadline) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (type != null) this.type = type;
        if (department != null) this.department = department;
        if (isTop != null) this.isTop = isTop;
        if (deadline != null) this.deadline = deadline;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 置顶 / 取消置顶
     */
    public void toggleTop(boolean top) {
        this.isTop = top ? 1 : 0;
        this.updateTime = LocalDateTime.now();
    }
}
