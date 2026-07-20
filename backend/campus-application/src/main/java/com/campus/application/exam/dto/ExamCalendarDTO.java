package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 考试日历响应（7.2）
 */
@Data
@Builder
public class ExamCalendarDTO {

    private Integer year;
    private Integer month;
    private List<CalendarEvent> events;

    @Data
    @Builder
    public static class CalendarEvent {
        private String date;
        private Integer dayOfWeek;
        private List<DayExam> exams;
    }

    @Data
    @Builder
    public static class DayExam {
        private Long id;
        private String courseName;
        private String time;
        private String classroom;
        private String status;
    }
}
