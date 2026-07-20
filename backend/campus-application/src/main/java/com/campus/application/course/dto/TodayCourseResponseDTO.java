package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class TodayCourseResponseDTO {
    private String date;
    private Integer dayOfWeek;
    private Integer week;
    private String semester;
    private List<CourseItemDTO> courses;
}
