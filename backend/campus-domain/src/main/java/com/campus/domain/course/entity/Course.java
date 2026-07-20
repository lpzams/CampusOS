package com.campus.domain.course.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Course {

    private Long id;
    private String name;
    private String courseCode;
    private Integer credit;
    private Long teacherId;
    private String teacherName;
    private String semester;
    private Integer maxStudents;
    private String color;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final String[] DAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
}
