package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 我的考试安排响应（7.9）
 */
@Data
@Builder
public class ExamMyDTO {

    private Integer total;
    private Integer pending;
    private Integer completed;
    private List<ExamItem> list;

    @Data
    @Builder
    public static class ExamItem {
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
        private String countdown;
    }
}
