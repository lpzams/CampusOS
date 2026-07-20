package com.campus.application.course.command;

import lombok.Data;

@Data
public class UpdateClassroomCommand {
    private String building;
    private String name;
    private Integer floor;
    private Integer capacity;
    private Integer type;
}
