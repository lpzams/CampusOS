package com.campus.api.controller.assistant;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated;
import com.campus.application.assistant.AssistantAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/ai")
public class AssistantController {
    private final AssistantAppService app; private final AuthSession session;
    public AssistantController(AssistantAppService app,AuthSession session){this.app=app;this.session=session;}
    @Authenticated @PostMapping("/chat") public Result<Map<String,Object>> chat(@RequestBody Map<String,Object> body){return Result.success(app.chat(session.userId(),body));}
    @GetMapping("/hot-questions") public Result<List<Map<String,Object>>> hot(){return Result.success(app.hotQuestions());}
    @PostMapping("/process") public Result<Map<String,Object>> process(@RequestBody Map<String,Object> body){return Result.success(app.process(String.valueOf(body.getOrDefault("type","")),String.valueOf(body.getOrDefault("action",""))));}
    @Authenticated @GetMapping("/recommend") public Result<List<Map<String,Object>>> recommend(@RequestParam String type){return Result.success(app.recommend(session.userId(),type));}
}
