package com.campus.api.controller.course;

import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.course.TeachingAppService;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Authenticated @RoleAllowed({3}) @RestController @RequestMapping("/api/admin/teaching")
public class AdminTeachingController {
    private final TeachingAppService app;
    public AdminTeachingController(TeachingAppService app) { this.app = app; }
    @GetMapping("/courses") public Result<List<Map<String,Object>>> courses(){ return Result.success(app.allCourses()); }
    @GetMapping("/teachers") public Result<List<Map<String,Object>>> teachers(){ return Result.success(app.teachers()); }
    @PostMapping("/courses") public Result<Map<String,Object>> arrange(@RequestBody Map<String,Object> body){ return Result.success(app.arrange(body)); }
    @PutMapping("/courses/{courseId}") public Result<Map<String,Object>> update(@PathVariable Long courseId,@RequestBody Map<String,Object> body){ return Result.success(app.reschedule(courseId, body)); }
    @DeleteMapping("/courses/{courseId}") public Result<Void> delete(@PathVariable Long courseId){ app.removeCourse(courseId); return Result.success(); }
}
