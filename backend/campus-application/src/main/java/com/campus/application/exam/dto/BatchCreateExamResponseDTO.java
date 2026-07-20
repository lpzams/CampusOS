package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量创建考试响应（7.6）
 */
@Data
@Builder
public class BatchCreateExamResponseDTO {

    private Integer successCount;
    private Integer failCount;
    private List<Long> ids;
}
