package com.campus.application.exam.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量创建考试安排命令（7.6）
 */
@Data
public class BatchCreateExamCommand {

    @NotEmpty(message = "考试列表不能为空")
    @Valid
    private List<CreateExamCommand> exams;
}
