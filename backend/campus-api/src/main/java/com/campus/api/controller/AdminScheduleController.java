package com.campus.api.controller;

import com.campus.application.course.command.*;
import com.campus.application.course.service.ScheduleAdminService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j @RestController @RequestMapping("/api/admin/schedule") @RequiredArgsConstructor
@Tag(name = "排课管理(管理员)", description = "排课CRUD")
public class AdminScheduleController {

    private final ScheduleAdminService scheduleAdminService;

    @PostMapping
    @Operation(summary = "5.B1 排课")
    public Result<Map<String, Object>> schedule(@Valid @RequestBody ScheduleCourseCommand cmd) {
        return Result.success(scheduleAdminService.schedule(cmd));
    }

    @PostMapping("/batch")
    @Operation(summary = "5.B2 批量排课")
    public Result<Map<String, Integer>> batch(@Valid @RequestBody BatchScheduleCommand cmd) {
        return Result.success(scheduleAdminService.batchSchedule(cmd));
    }

    @GetMapping("/list")
    @Operation(summary = "5.B3 排课列表")
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(required = false) String semester,
                                                         @RequestParam(required = false) Long teacherId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(scheduleAdminService.getScheduleList(semester, teacherId, page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "5.B4 取消排课")
    public Result<Void> cancel(@PathVariable Long id) {
        scheduleAdminService.cancelSchedule(id);
        return Result.success();
    }
}
