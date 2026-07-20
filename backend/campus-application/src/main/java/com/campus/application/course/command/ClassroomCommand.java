package com.campus.application.course.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassroomCommand {
    @NotBlank private String building;
    @NotBlank private String name;
    private Integer floor;
    private Integer capacity;
    /** 教室类型：1-普通 2-多媒体 3-智慧 4-实验室 5-机房 */
    private Integer type;
}
