package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class FreeClassroomDTO {
    private Long id;
    private String classroom;
    private String building;
    private Integer floor;
    private Integer capacity;
    /** 教室类型名称，如"多媒体教室" */
    private String type;
    private String status;
}
