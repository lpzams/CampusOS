package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CourseItemDTO {
    private Long id;
    private String name;
    private String teacher;
    private String classroom;
    private String building;
    private String timeSlot;
    private String weeks;
    private String color;
    private Integer credit;
    private String status;
}
