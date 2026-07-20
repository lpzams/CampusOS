package com.campus.domain.exam.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Exam {

    private Long id;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private LocalDate examDate;
    private String examTime;
    private String building;
    private String classroom;
    private String examType;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public static final String TYPE_FINAL = "FINAL";
    public static final String TYPE_MIDTERM = "MIDTERM";
    public static final String TYPE_MAKEUP = "MAKEUP";

    // ========== 业务方法 ==========

    public String getStatusDesc() {
        if (STATUS_PENDING.equals(status)) return "待考试";
        if (STATUS_COMPLETED.equals(status)) return "已考完";
        return status;
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    // ========== 工厂方法 ==========

    public static Exam create(Long courseId, String courseName, String courseCode,
                              LocalDate examDate, String examTime, String building,
                              String classroom, String examType) {
        Exam exam = new Exam();
        exam.setCourseId(courseId);
        exam.setCourseName(courseName);
        exam.setCourseCode(courseCode);
        exam.setExamDate(examDate);
        exam.setExamTime(examTime);
        exam.setBuilding(building);
        exam.setClassroom(classroom);
        exam.setExamType(examType != null ? examType : TYPE_FINAL);
        exam.setStatus(STATUS_PENDING);
        exam.setCreateTime(LocalDateTime.now());
        exam.setUpdateTime(LocalDateTime.now());
        return exam;
    }

    /**
     * 更新考试安排
     */
    public void updateFields(LocalDate examDate, String examTime, String classroom, String building) {
        if (examDate != null) this.examDate = examDate;
        if (examTime != null) this.examTime = examTime;
        if (classroom != null) this.classroom = classroom;
        if (building != null) this.building = building;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为已完成
     */
    public void markCompleted() {
        this.status = STATUS_COMPLETED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 校验考试类型合法
     */
    public static void validateType(String type) {
        if (type == null || (!TYPE_FINAL.equals(type) && !TYPE_MIDTERM.equals(type) && !TYPE_MAKEUP.equals(type))) {
            throw new IllegalArgumentException("考试类型不合法，仅支持 FINAL / MIDTERM / MAKEUP");
        }
    }
}
