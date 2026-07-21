package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 5.3 空闲教室响应
 */
@Data
@Builder
public class FreeClassroomResponseDTO {

    private String date;
    private String timeSlot;
    private String building;
    private List<FreeClassroomDTO> freeClassrooms;
}
