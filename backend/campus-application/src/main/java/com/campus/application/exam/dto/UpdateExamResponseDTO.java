package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 更新考试响应（7.4）
 */
@Data
@Builder
public class UpdateExamResponseDTO {

    private Long id;
    private String courseName;
    private String examDate;
    private String examTime;
    private String classroom;
    private String updateTime;
}
