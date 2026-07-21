package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 修改成绩响应（6.6）
 */
@Data
@Builder
public class UpdateScoreResponseDTO {

    private Long courseId;
    private String studentId;
    private Integer oldScore;
    private Integer newScore;
    private String updateTime;
}
