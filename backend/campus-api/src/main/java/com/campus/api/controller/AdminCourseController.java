package com.campus.api.controller;

import com.campus.application.course.command.*;
import com.campus.application.course.dto.*;
import com.campus.application.course.service.CourseAdminService;
import com.campus.application.course.service.CourseAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j @RestController @RequestMapping("/api/admin/course") @RequiredArgsConstructor
@Tag(name = "课程管理(管理员)", description = "课程CRUD、分配教师、统计")
public class AdminCourseController {

    private final CourseAdminService courseAdminService;
    private final CourseAppService courseAppService;

    @PostMapping
    @Operation(summary = "5.4 创建课程")
    public Result<Map<String, Object>> create(@Valid @RequestBody CreateCourseCommand cmd) {
        return Result.success(courseAdminService.createCourse(cmd));
    }

    @PutMapping("/{id}")
    @Operation(summary = "5.5 更新课程")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody UpdateCourseCommand cmd) {
        return Result.success(courseAdminService.updateCourse(id, cmd));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "5.6 删除课程")
    public Result<Void> delete(@PathVariable Long id) {
        courseAdminService.deleteCourse(id);
        return Result.success();
    }

    @PostMapping("/assign")
    @Operation(summary = "5.7 分配教师")
    public Result<Map<String, Object>> assign(@Valid @RequestBody AssignTeacherCommand cmd) {
        return Result.success(courseAdminService.assignTeacher(cmd));
    }

    @GetMapping("/list")
    @Operation(summary = "5.8 课程列表")
    public Result<PageResult<CourseListDTO>> list(@RequestParam(required = false) String semester,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(courseAdminService.getCourseList(semester, keyword, page, size));
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "5.C6 选课统计")
    public Result<CourseStatisticsDTO> statistics(@PathVariable Long id) {
        return Result.success(courseAppService.getCourseStatistics(id));
    }
}
