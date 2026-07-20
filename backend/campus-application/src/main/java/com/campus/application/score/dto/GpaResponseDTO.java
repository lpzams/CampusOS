package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * GPA响应（6.2）
 */
@Data
@Builder
public class GpaResponseDTO {

    private Double overallGpa;
    private Double currentSemesterGpa;
    private String semester;
    private Integer totalCredits;
    private Integer passedCredits;
    private Integer failedCredits;
    private Integer rank;
    private Integer totalStudents;
    private List<GpaHistoryDTO> history;

    @Data
    @Builder
    public static class GpaHistoryDTO {
        private String semester;
        private Double gpa;
        private Integer credits;
    }
}
