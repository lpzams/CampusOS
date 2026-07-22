package com.campus.api.controller.course;

import com.campus.api.auth.AuthSession;
import com.campus.api.auth.Authenticated;
import com.campus.application.course.CourseAppService;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/course")
public class CourseController {
    private final CourseAppService app; private final AuthSession session;
    public CourseController(CourseAppService app, AuthSession session) { this.app=app; this.session=session; }
    @Authenticated @GetMapping("/schedule") public Result<Map<String,Object>> schedule(@RequestParam(defaultValue="2026-2027-1") String semester,@RequestParam(defaultValue="1") int week){return Result.success(app.schedule(session.userId(),session.userType(),semester,week));}
    @Authenticated @GetMapping("/today") public Result<List<Map<String,Object>>> today(){return Result.success(app.today(session.userId(),session.userType()));}
    @GetMapping("/free-classrooms") public Result<List<Map<String,Object>>> free(@RequestParam(required=false) String building,@RequestParam(required=false) String date,@RequestParam(required=false) String timeSlot){return Result.success(app.freeClassrooms(building,date,timeSlot));}
    @GetMapping("/teacher/{teacherId}") public Result<List<Map<String,Object>>> teacher(@PathVariable Long teacherId){return Result.success(app.teacherCourses(teacherId));}
}
