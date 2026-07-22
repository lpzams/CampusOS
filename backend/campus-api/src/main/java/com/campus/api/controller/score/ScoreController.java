package com.campus.api.controller.score;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.score.ScoreAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.Map;

@Authenticated @RoleAllowed({1, 3}) @RestController @RequestMapping("/api/score")
public class ScoreController {
    private final ScoreAppService app; private final AuthSession session;
    public ScoreController(ScoreAppService app,AuthSession session){this.app=app;this.session=session;}
    @GetMapping("/list") public Result<Map<String,Object>> list(@RequestParam(required=false) String semester,@RequestParam(required=false) String type){return Result.success(app.list(session.userId(),semester,type));}
    @GetMapping("/gpa") public Result<Map<String,Object>> gpa(@RequestParam(required=false) String semester){return Result.success(app.gpa(session.userId(),semester));}
    @GetMapping("/statistics") public Result<Map<String,Object>> statistics(@RequestParam(required=false) String semester){return Result.success(app.statistics(session.userId(),semester));}
}
