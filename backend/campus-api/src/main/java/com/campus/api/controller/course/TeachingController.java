package com.campus.api.controller.course;

import com.campus.api.auth.AuthSession;
import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.course.TeachingAppService;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Authenticated @RestController @RequestMapping("/api/teaching")
public class TeachingController {
    private final TeachingAppService app; private final AuthSession session;
    public TeachingController(TeachingAppService app, AuthSession session) { this.app = app; this.session = session; }
    @RoleAllowed({1,3}) @GetMapping("/catalog") public Result<List<Map<String,Object>>> catalog(){ return Result.success(app.catalog()); }
    @RoleAllowed({1,3}) @GetMapping("/enrollments/my") public Result<List<Map<String,Object>>> mine(){ return Result.success(app.mine(session.userId())); }
    @RoleAllowed({1,3}) @PostMapping("/enrollments") public Result<Void> enroll(@RequestBody Map<String,String> body){ app.enroll(session.userId(), body.get("courseSourceId")); return Result.success(); }
    @RoleAllowed({1,3}) @DeleteMapping("/enrollments") public Result<Void> cancel(@RequestBody Map<String,String> body){ app.cancel(session.userId(), body.get("courseSourceId")); return Result.success(); }
    @RoleAllowed({2,3}) @GetMapping("/courses/my") public Result<List<Map<String,Object>>> courses(){ return Result.success(app.teacherCourses(session.userId())); }
    @RoleAllowed({2,3}) @GetMapping("/catalog-courses/my") public Result<List<Map<String,Object>>> catalogCourses(){ return Result.success(app.catalogTeacherCourses(session.userId())); }
    @RoleAllowed({2,3}) @GetMapping("/catalog-courses/{sourceId}/students") public Result<List<Map<String,Object>>> catalogStudents(@PathVariable String sourceId){ return Result.success(app.catalogStudents(session.userId(), sourceId)); }
    @RoleAllowed({2,3}) @GetMapping("/courses/{courseId}/students") public Result<List<Map<String,Object>>> students(@PathVariable Long courseId){ return Result.success(app.students(session.userId(), courseId)); }
    @RoleAllowed({2,3}) @GetMapping("/courses/{courseId}/reviews") public Result<List<Map<String,Object>>> reviews(@PathVariable Long courseId){ return Result.success(app.reviews(session.userId(), courseId)); }
    @RoleAllowed({2,3}) @PutMapping("/courses/{courseId}/students/{studentId}/score") public Result<Map<String,Object>> grade(@PathVariable Long courseId,@PathVariable Long studentId,@RequestBody Map<String,Number> body){ return Result.success(app.grade(session.userId(), courseId, studentId, body.get("score"))); }
}
