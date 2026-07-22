package com.campus.api.controller.course;

import com.campus.application.course.CourseReviewAppService;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course-reviews")
public class CourseReviewController {
    private final CourseReviewAppService appService;

    public CourseReviewController(CourseReviewAppService appService) {
        this.appService = appService;
    }

    @GetMapping("/catalog")
    public Result<List<Map<String, Object>>> catalog(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(appService.catalog(keyword, page, size));
    }

    @GetMapping("/{courseSourceId}")
    public Result<List<Map<String, Object>>> reviews(
            @PathVariable String courseSourceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(appService.reviews(courseSourceId, page, size));
    }
}
