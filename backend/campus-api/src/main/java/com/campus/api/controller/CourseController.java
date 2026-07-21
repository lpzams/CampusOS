package com.campus.api.controller;

import com.campus.application.course.command.SelectCourseCommand;
import com.campus.application.course.dto.*;
import com.campus.application.course.service.CourseAppService;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j @RestController @RequestMapping("/api/course") @RequiredArgsConstructor
@Tag(name = "课程管理", description = "课表、选课、教师授课相关接口")
public class CourseController {

    private final CourseAppService courseAppService;

    @GetMapping("/schedule")
    @Operation(summary = "5.1 获取个人课表")
    public Result<ScheduleResponseDTO> getSchedule(@RequestParam(required = false) String semester,
                                                    @RequestParam(required = false) Integer week) {
        return Result.success(courseAppService.getSchedule(UserAppService.getCurrentUserId(), semester, week));
    }

    @GetMapping("/today")
    @Operation(summary = "5.2 获取今日课程")
    public Result<TodayCourseResponseDTO> getToday() {
        return Result.success(courseAppService.getTodayCourses(UserAppService.getCurrentUserId()));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "5.3 获取教师授课列表")
    public Result<TeacherCourseResponseDTO> getTeacherCourses(@PathVariable Long teacherId,
                                                               @RequestParam(required = false) String semester) {
        return Result.success(courseAppService.getTeacherCourses(teacherId, semester));
    }

    @GetMapping("/my")
    @Operation(summary = "5.9 获取我的课程（教师）")
    public Result<List<CourseListDTO>> getMyCourses() {
        return Result.success(courseAppService.getMyCourses(UserAppService.getCurrentUserId()));
    }

    @PostMapping("/select")
    @Operation(summary = "5.C1 选课")
    public Result<Map<String, Object>> selectCourse(@Valid @RequestBody SelectCourseCommand cmd) {
        return Result.success(courseAppService.selectCourse(UserAppService.getCurrentUserId(), cmd));
    }

    @DeleteMapping("/drop/{courseId}")
    @Operation(summary = "5.C2 退课")
    public Result<Void> dropCourse(@PathVariable Long courseId) {
        courseAppService.dropCourse(UserAppService.getCurrentUserId(), courseId);
        return Result.success();
    }

    @GetMapping("/available")
    @Operation(summary = "5.C3 可选课程列表")
    public Result<PageResult<CourseListDTO>> getAvailable(@RequestParam(required = false) String semester,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(courseAppService.getAvailableCourses(UserAppService.getCurrentUserId(), semester, keyword, page, size));
    }

    @GetMapping("/my-selections")
    @Operation(summary = "5.C4 我的选课列表")
    public Result<List<Map<String, Object>>> getMySelections(@RequestParam(required = false) String semester) {
        return Result.success(courseAppService.getMySelections(UserAppService.getCurrentUserId(), semester));
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "5.C5 课程学生名单")
    public Result<StudentSelectionDTO> getCourseStudents(@PathVariable Long id) {
        return Result.success(courseAppService.getCourseStudents(id));
    }
}
