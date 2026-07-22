package com.campus.api.controller.news;

import com.campus.api.auth.AuthSession;
import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.news.NewsAppService;
import com.campus.application.news.command.AdminCreateNewsCommand;
import com.campus.common.api.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Authenticated @RoleAllowed({3}) @RestController @RequestMapping("/api/admin/news")
public class AdminNewsController {
    private final NewsAppService app; private final AuthSession session;
    public AdminNewsController(NewsAppService app, AuthSession session) { this.app = app; this.session = session; }
    @PostMapping public Result<Long> create(@Valid @RequestBody AdminCreateNewsCommand command) {
        return Result.success(app.createByAdmin(session.userType(), command));
    }
}
