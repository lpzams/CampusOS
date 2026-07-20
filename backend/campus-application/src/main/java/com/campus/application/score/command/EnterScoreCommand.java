package com.campus.application.score.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 录入成绩命令（6.4）
 */
@Data
public class EnterScoreCommand {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotNull(message = "成绩不能为空")
    @Min(value = 0, message = "成绩不能小于0")
    @Max(value = 100, message = "成绩不能大于100")
    private Integer score;

    @NotBlank(message = "成绩类型不能为空")
    private String type;
}
