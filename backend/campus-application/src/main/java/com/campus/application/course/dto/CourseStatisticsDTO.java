package com.campus.application.course.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data @Builder
public class CourseStatisticsDTO {
    private Long courseId;
    private String courseName;
    private Integer totalStudents;
    private Integer maxStudents;
    private Integer remainingSlots;
    private List<DepartmentCount> byDepartment;
    private List<ClassCount> byClass;

    @Data @Builder
    public static class DepartmentCount {
        private String department;
        private Integer count;
    }
    @Data @Builder
    public static class ClassCount {
        private String className;
        private Integer count;
    }
}
