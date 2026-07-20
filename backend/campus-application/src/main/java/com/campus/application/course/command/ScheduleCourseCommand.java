package com.campus.application.course.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleCourseCommand {
    @NotNull private Long courseId;
    @NotNull private Long classroomId;
    @NotNull private Integer dayOfWeek;
    @NotBlank private String timeSlot;
    @NotBlank private String weeks;
}
