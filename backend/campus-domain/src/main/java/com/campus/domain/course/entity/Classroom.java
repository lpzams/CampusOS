package com.campus.domain.course.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Classroom {

    private Long id;
    private String building;
    private String name;
    private Integer floor;
    private Integer capacity;
    /** 教室类型：1-普通 2-多媒体 3-智慧 4-实验室 5-机房 */
    private Integer type;
    /** '行政状态：0-空闲 1-占用 2-停用' */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getTypeName() {
        return ClassroomType.getName(type != null ? type : 1);
    }

    public String getStatusName() {
        return ClassroomStatus.getName(status != null ? status : 0);
    }

    /** 是否可用（正常状态的教室才能排课） */
    public boolean isAvailable() {
        return status == null || status == ClassroomStatus.FREE;
    }
}
