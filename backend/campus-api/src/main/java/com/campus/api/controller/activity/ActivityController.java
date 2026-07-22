package com.campus.api.controller.activity;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.activity.ActivityAppService; import com.campus.application.shared.Values;
import com.campus.common.api.PageResult; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/activity")
public class ActivityController {
    private final ActivityAppService app; private final AuthSession session;
    public ActivityController(ActivityAppService app,AuthSession session){this.app=app;this.session=session;}
    @GetMapping("/list") public Result<PageResult<Map<String,Object>>> list(@RequestParam(required=false) String category,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="10") int size){return Result.success(app.page(category,status,page,size));}
    @GetMapping("/detail/{id}") public Result<Map<String,Object>> detail(@PathVariable Long id){return Result.success(app.detail(id));}
    @Authenticated @RoleAllowed({1, 3}) @PostMapping("/register") public Result<Map<String,Object>> register(@RequestBody Map<String,Object> body){return Result.success(app.register(session.userId(),Values.longValue(body.get("activityId"))));}
    @Authenticated @RoleAllowed({1, 3}) @DeleteMapping("/register/{id}") public Result<Void> cancel(@PathVariable Long id){app.cancel(session.userId(),id);return Result.success();}
    @Authenticated @RoleAllowed({1, 3}) @PostMapping("/checkin") public Result<Map<String,Object>> checkin(@RequestBody Map<String,Object> body){return Result.success(app.checkIn(session.userId(),Values.longValue(body.get("activityId")),String.valueOf(body.getOrDefault("code",""))));}
    @Authenticated @RoleAllowed({1, 3}) @GetMapping("/my") public Result<List<Map<String,Object>>> mine(){return Result.success(app.mine(session.userId()));}
}
