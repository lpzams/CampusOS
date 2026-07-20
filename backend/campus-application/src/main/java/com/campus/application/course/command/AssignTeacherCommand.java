package com.campus.application.course.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTeacherCommand {
    @NotNull private Long courseId;
    @NotNull private Long teacherId;
    @NotNull private String semester;
}
