package com.campus.api.controller;

import com.campus.application.score.command.ScoreListQuery;
import com.campus.application.score.dto.GpaResponseDTO;
import com.campus.application.score.dto.ScoreListResponseDTO;
import com.campus.application.score.dto.ScoreStatisticsDTO;
import com.campus.application.score.service.ScoreAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
@Tag(name = "成绩查询", description = "学生成绩查询、GPA、统计分析相关接口")
public class ScoreController {

    private final ScoreAppService scoreAppService;

    // ==================== 6.1 获取成绩列表 ====================

    @GetMapping("/list")
    @Operation(summary = "获取成绩列表")
    public Result<ScoreListResponseDTO> getScoreList(ScoreListQuery query) {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取成绩列表: userId={}, semester={}, type={}", userId, query.getSemester(), query.getType());
        return Result.success(scoreAppService.getScoreList(userId, query));
    }

    // ==================== 6.2 获取GPA ====================

    @GetMapping("/gpa")
    @Operation(summary = "获取GPA")
    public Result<GpaResponseDTO> getGpa() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("获取GPA: userId={}", userId);
        return Result.success(scoreAppService.getGpa(userId));
    }

    // ==================== 6.3 成绩统计分析 ====================

    @GetMapping("/statistics")
    @Operation(summary = "成绩统计分析")
    public Result<ScoreStatisticsDTO> getScoreStatistics() {
        Long userId = UserAppService.getCurrentUserId();
        log.info("成绩统计分析: userId={}", userId);
        return Result.success(scoreAppService.getScoreStatistics(userId));
    }
}
