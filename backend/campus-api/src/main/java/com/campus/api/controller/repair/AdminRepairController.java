package com.campus.api.controller.repair;

import com.campus.api.auth.Authenticated;
import com.campus.api.auth.RoleAllowed;
import com.campus.application.repair.RepairAppService;
import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Authenticated @RoleAllowed({3}) @RestController @RequestMapping("/api/admin/repair")
public class AdminRepairController {
    private final RepairAppService app;
    public AdminRepairController(RepairAppService app) { this.app = app; }
    @GetMapping public Result<List<Map<String, Object>>> list() { return Result.success(app.all()); }
    @PutMapping("/{id}/progress") public Result<Map<String, Object>> progress(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return Result.success(app.progress(id, String.valueOf(body.get("status")), String.valueOf(body.getOrDefault("statusDesc", ""))));
    }
}
