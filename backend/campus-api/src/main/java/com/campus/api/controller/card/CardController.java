package com.campus.api.controller.card;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.card.CardAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@Authenticated @RoleAllowed({1, 3}) @RestController @RequestMapping("/api/card")
public class CardController {
    private final CardAppService app; private final AuthSession session;
    public CardController(CardAppService app,AuthSession session){this.app=app;this.session=session;}
    @GetMapping("/info") public Result<Map<String,Object>> info(){return Result.success(app.info(session.userId()));}
    @GetMapping("/transactions") public Result<List<Map<String,Object>>> transactions(){return Result.success(app.transactions(session.userId()));}
    @PostMapping("/loss") public Result<Map<String,Object>> loss(){return Result.success(app.reportLoss(session.userId()));}
    @PostMapping("/unloss") public Result<Map<String,Object>> unloss(){return Result.success(app.cancelLoss(session.userId()));}
    @PostMapping("/recharge") public Result<Map<String,Object>> recharge(@RequestBody Map<String,Object> body){return Result.success(app.recharge(session.userId(),body));}
}
