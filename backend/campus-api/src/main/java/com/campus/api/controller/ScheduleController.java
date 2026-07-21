package com.campus.api.controller;

import com.campus.application.course.service.ScheduleAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j @RestController @RequestMapping("/api/admin/schedule") @RequiredArgsConstructor
@Tag(name = "排课管理", description = "排课调度相关接口")
public class ScheduleController {

    // 排课相关端点在 AdminScheduleController 中
    // 此 Controller 保留作为排课模块入口，5.B5 空闲教室已迁移至 ClassroomController
}
