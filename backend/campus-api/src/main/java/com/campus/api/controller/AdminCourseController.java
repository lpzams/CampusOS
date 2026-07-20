package com.campus.api.controller;

import com.campus.application.score.service.CourseAdminService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
@Tag(name = "课程管理", description = "管理员课程创建、更新、删除、列表相关接口")
public class AdminCourseController {

    private final CourseAdminService courseAdminService;

    // ==================== 5.5 创建课程（管理员） ====================

    @PostMapping
    @Operation(summary = "创建课程（管理员）")
    public Result<Map<String, Object>> createCourse(@RequestBody Map<String, Object> command) {
        log.info("创建课程: name={}, courseCode={}", command.get("name"), command.get("courseCode"));
        return Result.success(courseAdminService.createCourse(command));
    }

    // ==================== 5.6 更新课程（管理员） ====================

    @PutMapping("/{id}")
    @Operation(summary = "更新课程（管理员）")
    public Result<Map<String, Object>> updateCourse(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> command) {
        log.info("更新课程: id={}", id);
        return Result.success(courseAdminService.updateCourse(id, command));
    }

    // ==================== 5.7 删除课程（管理员） ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程（管理员）")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        log.info("删除课程: id={}", id);
        courseAdminService.deleteCourse(id);
        return Result.success();
    }

    // ==================== 5.9 获取课程列表（管理员） ====================

    @GetMapping("/list")
    @Operation(summary = "获取课程列表（管理员）")
    public Result<PageResult<Map<String, Object>>> getCourseList(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取课程列表: semester={}, keyword={}, page={}, size={}", semester, keyword, page, size);
        return Result.success(courseAdminService.getCourseList(semester, keyword, page, size));
    }
}
