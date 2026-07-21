package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ClassroomListDTO {
    private Long id;
    private String building;
    private String name;
    private Integer floor;
    private Integer capacity;
    private Integer type;
    private String typeName;
    /** 行政状态文本：空闲/占用/停用 */
    private String status;
}
