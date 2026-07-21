package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 成绩统计分析响应（6.3）
 */
@Data
@Builder
public class ScoreStatisticsDTO {

    private Integer totalCourses;
    private Double avgScore;
    private Integer maxScore;
    private Integer minScore;
    private Map<String, Integer> distribution;
    private List<SemesterStatDTO> bySemester;
    private Map<String, Double> byCategory;

    @Data
    @Builder
    public static class SemesterStatDTO {
        private String semester;
        private Double avgScore;
        private Integer credit;
    }
}
