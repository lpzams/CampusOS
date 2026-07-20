package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 考试安排条目（7.1 列表项）
 */
@Data
@Builder
public class ExamDTO {

    private Long id;
    private String courseName;
    private String courseCode;
    private String examDate;
    private String examTime;
    private String building;
    private String classroom;
    private String seatNumber;
    private String status;
    private String statusCode;
}
