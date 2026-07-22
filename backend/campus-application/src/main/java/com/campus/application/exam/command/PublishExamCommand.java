package com.campus.application.exam.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PublishExamCommand {
    @NotBlank private String courseName;
    private String courseCode;
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") private String examDate;
    @NotBlank private String examTime;
    @NotBlank private String building;
    @NotBlank private String classroom;
    private String seatNumber;
    private Long userId;
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }
    public String getExamTime() { return examTime; }
    public void setExamTime(String examTime) { this.examTime = examTime; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
