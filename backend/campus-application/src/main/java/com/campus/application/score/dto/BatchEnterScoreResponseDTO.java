package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 批量录入成绩响应（6.5）
 */
@Data
@Builder
public class BatchEnterScoreResponseDTO {

    private Integer successCount;
    private Integer failCount;
}
