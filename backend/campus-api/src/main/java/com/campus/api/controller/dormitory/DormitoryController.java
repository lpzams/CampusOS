package com.campus.api.controller.dormitory;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.dormitory.DormitoryAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/dormitory")
public class DormitoryController {
    private final DormitoryAppService app; private final AuthSession session;
    public DormitoryController(DormitoryAppService app,AuthSession session){this.app=app;this.session=session;}
    @Authenticated @RoleAllowed({1, 3}) @GetMapping("/info") public Result<Map<String,Object>> info(){return Result.success(app.info(session.userId()));}
    @GetMapping("/notice") public Result<List<Map<String,Object>>> notice(){return Result.success(app.notices());}
    @Authenticated @RoleAllowed({1, 3}) @GetMapping("/utility") public Result<Map<String,Object>> utility(){return Result.success(app.utility(session.userId()));}
}
