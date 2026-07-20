package com.campus.application.course.command;

import lombok.Data;

@Data
public class UpdateCourseCommand {
    private String name;
    private String courseCode;
    private Integer credit;
    private Long teacherId;
    private String semester;
    private String classroom;
    private String building;
    private String timeSlot;
    private String weeks;
    private Integer maxStudents;
}
