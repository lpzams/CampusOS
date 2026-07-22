package com.campus.api.controller.course;

import com.campus.api.auth.AuthSession;
import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.course.CourseSelectionAppService;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course-selection")
@Authenticated
@RoleAllowed({1,3})
public class CourseSelectionController {
    private final CourseSelectionAppService appService;
    private final AuthSession session;

    public CourseSelectionController(CourseSelectionAppService appService, AuthSession session) {
        this.appService = appService;
        this.session = session;
    }

    @GetMapping("/my")
    @RoleAllowed({1, 3})
    public Result<List<Map<String, Object>>> mine() {
        return Result.success(appService.mine(session.userId()));
    }

    @PostMapping
    @RoleAllowed({1, 3})
    public Result<Void> select(@RequestBody Map<String, String> body) {
        appService.select(session.userId(), body.get("courseSourceId"));
        return Result.success();
    }

    @DeleteMapping
    @RoleAllowed({1, 3})
    public Result<Void> cancel(@RequestBody Map<String, String> body) {
        appService.cancel(session.userId(), body.get("courseSourceId"));
        return Result.success();
    }
}
