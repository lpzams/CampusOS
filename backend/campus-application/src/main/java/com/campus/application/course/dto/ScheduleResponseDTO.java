package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class ScheduleResponseDTO {
    private String semester;
    private Integer week;
    private String studentId;
    private List<ScheduleSlotDTO> schedule;
}
