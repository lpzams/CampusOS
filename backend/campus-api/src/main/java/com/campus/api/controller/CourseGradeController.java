package com.campus.api.controller;

import com.campus.application.score.command.*;
import com.campus.application.score.dto.*;
import com.campus.application.score.service.ScoreAdminService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
@Tag(name = "成绩管理", description = "教师/管理员成绩录入、修改、查询、导出相关接口")
public class CourseGradeController {

    private final ScoreAdminService scoreAdminService;

    // ==================== 6.4 录入成绩（教师） ====================

    @PostMapping("/grade")
    @Operation(summary = "录入成绩（教师）")
    public Result<EnterScoreResponseDTO> enterScore(@Valid @RequestBody EnterScoreCommand command) {
        log.info("录入成绩: courseId={}, studentId={}, score={}, type={}",
                command.getCourseId(), command.getStudentId(), command.getScore(), command.getType());
        return Result.success(scoreAdminService.enterScore(command));
    }

    // ==================== 6.5 批量录入成绩（教师） ====================

    @PostMapping("/grade/batch")
    @Operation(summary = "批量录入成绩（教师）")
    public Result<BatchEnterScoreResponseDTO> batchEnterScore(@Valid @RequestBody BatchEnterScoreCommand command) {
        log.info("批量录入成绩: courseId={}, type={}, count={}",
                command.getCourseId(), command.getType(), command.getGrades().size());
        BatchEnterScoreResponseDTO result = scoreAdminService.batchEnterScore(command);
        return Result.success(result);
    }

    // ==================== 6.6 修改成绩（教师） ====================

    @PutMapping("/grade")
    @Operation(summary = "修改成绩（教师）")
    public Result<UpdateScoreResponseDTO> updateScore(@Valid @RequestBody UpdateScoreCommand command) {
        log.info("修改成绩: courseId={}, studentId={}, newScore={}",
                command.getCourseId(), command.getStudentId(), command.getScore());
        return Result.success(scoreAdminService.updateScore(command));
    }

    // ==================== 6.7 获取课程成绩列表（教师/管理员） ====================

    @GetMapping("/{courseId}/grades")
    @Operation(summary = "获取课程成绩列表（教师/管理员）")
    public Result<PageResult<CourseGradeDTO>> getCourseGrades(@PathVariable Long courseId,
                                                               CourseGradeListQuery query) {
        log.info("获取课程成绩列表: courseId={}, type={}, page={}, size={}",
                courseId, query.getType(), query.getPageNum(), query.getPageSize());
        return Result.success(scoreAdminService.getCourseGrades(courseId, query));
    }

    // ==================== 6.8 成绩统计（教师/管理员） ====================

    @GetMapping("/{courseId}/grade-statistics")
    @Operation(summary = "成绩统计（教师/管理员）")
    public Result<CourseGradeStatisticsDTO> getCourseGradeStatistics(@PathVariable Long courseId) {
        log.info("课程成绩统计: courseId={}", courseId);
        return Result.success(scoreAdminService.getCourseGradeStatistics(courseId));
    }

    // ==================== 6.9 导出成绩（教师/管理员） ====================

    @GetMapping("/{courseId}/grades/export")
    @Operation(summary = "导出成绩（教师/管理员）")
    public ResponseEntity<byte[]> exportCourseGrades(@PathVariable Long courseId) {
        log.info("导出成绩: courseId={}", courseId);
        byte[] excelBytes = scoreAdminService.exportCourseGrades(courseId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                "成绩表_" + courseId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
