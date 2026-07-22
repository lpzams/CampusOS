package com.campus.api.controller.exam;

import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.exam.ExamAppService;
import com.campus.application.exam.command.PublishExamCommand;
import com.campus.common.api.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Authenticated @RoleAllowed({3}) @RestController @RequestMapping("/api/admin/exam")
public class AdminExamController {
    private final ExamAppService app;
    public AdminExamController(ExamAppService app) { this.app = app; }
    @PostMapping public Result<Long> publish(@Valid @RequestBody PublishExamCommand command) { return Result.success(app.publish(command)); }
}
