package com.campus.api.controller;

import com.campus.application.exam.command.ExamReminderCommand;
import com.campus.application.exam.dto.*;
import com.campus.application.exam.service.ExamAppService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
@Tag(name = "考试安排", description = "考试列表、日历、我的考试、提醒设置相关接口")
public class ExamController {

    private final ExamAppService examAppService;

    // ==================== 7.1 获取考试安排 ====================

    @GetMapping("/list")
    @Operation(summary = "获取考试安排")
    public Result<List<ExamDTO>> getExamList(@RequestParam(required = false) String status) {
        log.info("获取考试安排: status={}", status);
        return Result.success(examAppService.getExamList(status));
    }

    // ==================== 7.2 获取考试日历 ====================

    @GetMapping("/calendar")
    @Operation(summary = "获取考试日历")
    public Result<ExamCalendarDTO> getExamCalendar(@RequestParam String month) {
        // month 格式：YYYY-MM
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        log.info("获取考试日历: year={}, month={}", year, m);
        return Result.success(examAppService.getExamCalendar(year, m));
    }

    // ==================== 7.9 我的考试安排 ====================

    @GetMapping("/my")
    @Operation(summary = "我的考试安排")
    public Result<ExamMyDTO> getMyExams(@RequestParam(required = false) String status) {
        log.info("我的考试安排: status={}", status);
        return Result.success(examAppService.getMyExams(status));
    }

    // ==================== 7.10 考试提醒设置 ====================

    @GetMapping("/reminder")
    @Operation(summary = "获取考试提醒设置")
    public Result<ExamReminderDTO> getReminder() {
        log.info("获取考试提醒设置");
        return Result.success(examAppService.getReminder());
    }

    @PutMapping("/reminder")
    @Operation(summary = "更新考试提醒设置")
    public Result<ExamReminderDTO> updateReminder(@RequestBody ExamReminderCommand command) {
        log.info("更新考试提醒设置: enabled={}, minutes={}", command.getEnabled(), command.getReminderMinutes());
        return Result.success(examAppService.updateReminder(command));
    }
}
