package com.campus.application.score.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 成绩条目（6.1 列表中每条记录）
 */
@Data
@Builder
public class ScoreItemDTO {

    private String courseName;
    private String courseCode;
    private Integer credit;
    private Integer score;
    private String grade;
    private String type;
    private String examTime;
    private Boolean isPassed;
}
