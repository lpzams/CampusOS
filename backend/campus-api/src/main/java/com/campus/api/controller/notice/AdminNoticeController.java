package com.campus.api.controller.notice;

import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.notice.NoticeAppService;
import com.campus.application.notice.command.PublishNoticeCommand;
import com.campus.common.api.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Authenticated @RoleAllowed({3}) @RestController @RequestMapping("/api/admin/notice")
public class AdminNoticeController {
    private final NoticeAppService app;
    public AdminNoticeController(NoticeAppService app) { this.app = app; }
    @PostMapping public Result<Long> publish(@Valid @RequestBody PublishNoticeCommand command) { return Result.success(app.publish(command)); }
}
