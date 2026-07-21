package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 成绩列表响应（6.1）
 */
@Data
@Builder
public class ScoreListResponseDTO {

    private String semester;
    private Double gpa;
    private Integer totalCredits;
    private Integer passedCredits;
    private List<ScoreItemDTO> scores;
}
