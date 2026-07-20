package com.campus.application.course.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectCourseCommand {
    @NotNull private Long courseId;
}
