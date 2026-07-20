package com.campus.application.exam.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建考试安排命令（7.3）
 */
@Data
public class CreateExamCommand {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String courseCode;

    @NotBlank(message = "考试日期不能为空")
    private String examDate;

    @NotBlank(message = "考试时间不能为空")
    private String examTime;

    private String building;

    @NotBlank(message = "教室不能为空")
    private String classroom;

    private String examType;
}
