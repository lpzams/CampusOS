package com.campus.application.course.command;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchScheduleCommand {
    @NotEmpty private List<ScheduleCourseCommand> schedules;
}
