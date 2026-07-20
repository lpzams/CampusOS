package com.campus.application.score.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量录入成绩命令（6.5）
 */
@Data
public class BatchEnterScoreCommand {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "成绩类型不能为空")
    private String type;

    @NotEmpty(message = "成绩列表不能为空")
    @Valid
    private List<GradeItem> grades;

    @Data
    public static class GradeItem {

        @NotBlank(message = "学号不能为空")
        private String studentId;

        @NotNull(message = "成绩不能为空")
        @jakarta.validation.constraints.Min(value = 0, message = "成绩不能小于0")
        @jakarta.validation.constraints.Max(value = 100, message = "成绩不能大于100")
        private Integer score;
    }
}
