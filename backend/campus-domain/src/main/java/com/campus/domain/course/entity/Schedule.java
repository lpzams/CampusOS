package com.campus.domain.course.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Schedule {

    private Long id;
    private Long courseId;
    private Long classroomId;
    private Integer dayOfWeek;
    private String timeSlot;
    private String weeks;
    private LocalDateTime createTime;

    // 关联字段（查询时填充）
    private String courseName;
    private String courseCode;
    private Long teacherId;
    private String teacherName;
    private String classroom;
    private String building;

    public String getDayName() {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) return "未知";
        return Course.DAY_NAMES[dayOfWeek];
    }

    public boolean isActiveInWeek(int week) {
        if (weeks == null || weeks.isEmpty()) return true;
        try {
            String cleaned = weeks.replace("周", "").trim();
            String[] parts = cleaned.split("-");
            if (parts.length == 2) {
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                return week >= start && week <= end;
            }
        } catch (NumberFormatException ignored) {}
        return true;
    }
}
