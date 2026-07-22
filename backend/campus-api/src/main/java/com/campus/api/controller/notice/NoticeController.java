package com.campus.api.controller.notice;

import com.campus.api.auth.AuthSession;
import com.campus.api.auth.Authenticated;
import com.campus.application.notice.NoticeAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeAppService app; private final AuthSession session;
    public NoticeController(NoticeAppService app, AuthSession session) { this.app = app; this.session = session; }
    @GetMapping("/list") public Result<PageResult<Map<String,Object>>> list(@RequestParam(required=false) String type, @RequestParam(required=false) String department, @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="10") int size) { return Result.success(app.page(type, department, page, size)); }
    @GetMapping("/detail/{id}") public Result<Map<String,Object>> detail(@PathVariable Long id) { return Result.success(app.detail(id)); }
    @Authenticated @PostMapping("/read/{id}") public Result<Void> read(@PathVariable Long id) { app.markRead(session.userId(), id); return Result.success(); }
    @Authenticated @GetMapping("/unread/count") public Result<Map<String,Long>> unread() { return Result.success(Map.of("count", app.unreadCount(session.userId()))); }
}
