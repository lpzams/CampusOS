package com.campus.domain.score.entity;

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

    // ========== 工厂方法 ==========

    public static Course create(String name, String courseCode, Integer credit,
                                Long teacherId, String teacherName, String semester,
                                Integer maxStudents, String color) {
        Course course = new Course();
        course.setName(name);
        course.setCourseCode(courseCode);
        course.setCredit(credit != null ? credit : 0);
        course.setTeacherId(teacherId);
        course.setTeacherName(teacherName);
        course.setSemester(semester);
        course.setMaxStudents(maxStudents != null ? maxStudents : 60);
        course.setColor(color);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        return course;
    }

    // ========== 业务方法 ==========

    public void updateFields(String name, Integer credit, Integer maxStudents) {
        if (name != null) this.name = name;
        if (credit != null) this.credit = credit;
        if (maxStudents != null) this.maxStudents = maxStudents;
        this.updateTime = LocalDateTime.now();
    }
}
