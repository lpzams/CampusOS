package com.campus.api.controller.repair;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.repair.RepairAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@Authenticated @RoleAllowed({1, 3}) @RestController @RequestMapping("/api/repair")
public class RepairController {
    private final RepairAppService app; private final AuthSession session;
    public RepairController(RepairAppService app,AuthSession session){this.app=app;this.session=session;}
    @PostMapping("/submit") public Result<Map<String,Object>> submit(@RequestBody Map<String,Object> body){return Result.success(app.submit(session.userId(),body));}
    @GetMapping("/list") public Result<List<Map<String,Object>>> list(){return Result.success(app.list(session.userId()));}
    @GetMapping("/detail/{id}") public Result<Map<String,Object>> detail(@PathVariable Long id){return Result.success(app.detail(session.userId(),id));}
    @PostMapping("/evaluate") public Result<Map<String,Object>> evaluate(@RequestBody Map<String,Object> body){return Result.success(app.evaluate(session.userId(),body));}
}
