package com.campus.application.exam.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配考场座位命令（7.8）
 */
@Data
public class SeatAssignCommand {

    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotNull(message = "考场ID不能为空")
    private Long roomId;

    @NotEmpty(message = "学生ID列表不能为空")
    private List<String> studentIds;
}
