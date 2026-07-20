package com.campus.api.controller;

import com.campus.application.course.dto.FreeClassroomResponseDTO;
import com.campus.application.course.service.ClassroomAdminService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j @RestController @RequiredArgsConstructor
@Tag(name = "教室查询", description = "公开教室接口")
public class ClassroomController {

    private final ClassroomAdminService classroomAdminService;

    @GetMapping("/api/classroom/all")
    @Operation(summary = "5.A5 获取所有教室")
    public Result<List<Map<String, Object>>> getAll() {
        return Result.success(classroomAdminService.getAllClassrooms());
    }

    @GetMapping("/api/classroom/free")
    @Operation(summary = "5.A6 获取空闲教室")
    public Result<FreeClassroomResponseDTO> getFreeClassrooms(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timeSlot) {
        log.info("获取空闲教室: building={}, date={}, timeSlot={}", building, date, timeSlot);
        return Result.success(classroomAdminService.getFreeClassrooms(building, date, timeSlot));
    }
}
