package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 座位分配响应（7.8）
 */
@Data
@Builder
public class SeatAssignResponseDTO {

    private Long examId;
    private Long roomId;
    private Integer assignedCount;
    private String assignTime;
}
