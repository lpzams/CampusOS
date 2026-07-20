package com.campus.api.controller;

import com.campus.application.exam.command.*;
import com.campus.application.exam.dto.*;
import com.campus.application.exam.service.ExamAdminService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/exam")
@RequiredArgsConstructor
@Tag(name = "考试管理", description = "管理员考试创建、更新、删除、考场座位分配相关接口")
public class AdminExamController {

    private final ExamAdminService examAdminService;

    // ==================== 7.3 创建考试安排 ====================

    @PostMapping
    @Operation(summary = "创建考试安排（管理员）")
    public Result<CreateExamResponseDTO> createExam(@Valid @RequestBody CreateExamCommand command) {
        log.info("创建考试安排: courseName={}, examDate={}", command.getCourseName(), command.getExamDate());
        return Result.success(examAdminService.createExam(command));
    }

    // ==================== 7.4 更新考试安排 ====================

    @PutMapping("/{id}")
    @Operation(summary = "更新考试安排（管理员）")
    public Result<UpdateExamResponseDTO> updateExam(@PathVariable Long id,
                                                     @RequestBody UpdateExamCommand command) {
        log.info("更新考试安排: id={}", id);
        return Result.success(examAdminService.updateExam(id, command));
    }

    // ==================== 7.5 删除考试安排 ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "删除考试安排（管理员）")
    public Result<Void> deleteExam(@PathVariable Long id) {
        log.info("删除考试安排: id={}", id);
        examAdminService.deleteExam(id);
        return Result.success();
    }

    // ==================== 7.6 批量创建考试安排 ====================

    @PostMapping("/batch")
    @Operation(summary = "批量创建考试安排（管理员）")
    public Result<BatchCreateExamResponseDTO> batchCreateExam(@Valid @RequestBody BatchCreateExamCommand command) {
        log.info("批量创建考试安排: count={}", command.getExams().size());
        return Result.success(examAdminService.batchCreateExam(command));
    }

    // ==================== 7.7 获取考场安排 ====================

    @GetMapping("/rooms")
    @Operation(summary = "获取考场安排（管理员）")
    public Result<ExamRoomDTO> getExamRooms(@RequestParam Long examId) {
        log.info("获取考场安排: examId={}", examId);
        return Result.success(examAdminService.getExamRooms(examId));
    }

    // ==================== 7.8 分配考场座位 ====================

    @PostMapping("/seat-assign")
    @Operation(summary = "分配考场座位（管理员）")
    public Result<SeatAssignResponseDTO> assignSeats(@Valid @RequestBody SeatAssignCommand command) {
        log.info("分配考场座位: examId={}, roomId={}, count={}",
                command.getExamId(), command.getRoomId(), command.getStudentIds().size());
        return Result.success(examAdminService.assignSeats(command));
    }
}
