package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 课程成绩统计响应（6.8）
 */
@Data
@Builder
public class CourseGradeStatisticsDTO {

    private Long courseId;
    private String courseName;
    private String courseCode;
    private Integer totalStudents;
    private Double avgScore;
    private Integer maxScore;
    private Integer minScore;
    private Double passRate;
    private Map<String, Integer> distribution;
}
