package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 创建考试响应（7.3）
 */
@Data
@Builder
public class CreateExamResponseDTO {

    private Long id;
    private String courseName;
    private String examDate;
    private String examTime;
    private String classroom;
    private String status;
    private String statusCode;
    private String createTime;
}
