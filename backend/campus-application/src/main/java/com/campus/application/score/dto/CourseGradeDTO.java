package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 课程成绩条目（6.7 列表中每条记录 - 教师端视角）
 */
@Data
@Builder
public class CourseGradeDTO {

    private String studentId;
    private String realName;
    private String className;
    private Integer score;
    private String grade;
    private Boolean isPassed;
}
