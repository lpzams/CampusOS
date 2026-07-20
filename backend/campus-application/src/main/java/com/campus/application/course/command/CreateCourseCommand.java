package com.campus.application.course.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCourseCommand {
    @NotBlank private String name;
    @NotBlank private String courseCode;
    @NotNull private Integer credit;
    @NotNull private Long teacherId;
    @NotBlank private String semester;
    @NotBlank private String classroom;
    @NotBlank private String building;
    @NotBlank private String timeSlot;
    @NotBlank private String weeks;
    private Integer maxStudents;
}
