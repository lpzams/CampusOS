package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class ScheduleSlotDTO {
    private Integer dayOfWeek;
    private String dayName;
    private String timeSlot;
    private List<CourseItemDTO> courses;
}
