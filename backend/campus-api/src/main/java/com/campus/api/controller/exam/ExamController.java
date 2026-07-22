package com.campus.api.controller.exam;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.exam.ExamAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@Authenticated @RoleAllowed({1, 3}) @RestController @RequestMapping("/api/exam")
public class ExamController {
    private final ExamAppService app; private final AuthSession session;
    public ExamController(ExamAppService app,AuthSession session){this.app=app;this.session=session;}
    @GetMapping("/list") public Result<List<Map<String,Object>>> list(){return Result.success(app.list(session.userId()));}
    @GetMapping("/calendar") public Result<List<Map<String,Object>>> calendar(@RequestParam String month){return Result.success(app.calendar(session.userId(),month));}
}
