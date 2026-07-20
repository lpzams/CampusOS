package com.campus.application.exam.command;

import lombok.Data;

/**
 * 更新考试安排命令（7.4）
 */
@Data
public class UpdateExamCommand {

    private String examDate;
    private String examTime;
    private String classroom;
    private String building;
}
