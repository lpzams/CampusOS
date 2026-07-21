package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 录入成绩响应（6.4）
 */
@Data
@Builder
public class EnterScoreResponseDTO {

    private Long courseId;
    private String studentId;
    private Integer score;
    private String type;
    private String updateTime;
}
