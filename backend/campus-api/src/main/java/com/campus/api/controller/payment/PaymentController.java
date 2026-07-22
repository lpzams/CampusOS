package com.campus.api.controller.payment;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.payment.PaymentAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@Authenticated @RoleAllowed({1, 3}) @RestController @RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentAppService app; private final AuthSession session;
    public PaymentController(PaymentAppService app,AuthSession session){this.app=app;this.session=session;}
    @GetMapping("/pending") public Result<List<Map<String,Object>>> pending(){return Result.success(app.pending(session.userId()));}
    @PostMapping("/order") public Result<Map<String,Object>> order(@RequestBody Map<String,Object> body){return Result.success(app.createOrder(session.userId(),body));}
    @GetMapping("/records") public Result<List<Map<String,Object>>> records(){return Result.success(app.records(session.userId()));}
    @PostMapping("/electricity") public Result<Map<String,Object>> electricity(@RequestBody Map<String,Object> body){return Result.success(app.rechargeElectricity(session.userId(),body));}
}
