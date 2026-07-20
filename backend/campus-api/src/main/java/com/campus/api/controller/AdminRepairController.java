package com.campus.api.controller;

import com.campus.application.repair.service.RepairAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/repair")
@RequiredArgsConstructor
@Tag(name = "报修管理", description = "管理员报修列表、状态更新相关接口")
public class AdminRepairController {

    private final RepairAppService repairAppService;

    @GetMapping("/list")
    @Operation(summary = "报修列表（管理员）")
    public Result<PageResult<Map<String, Object>>> getAdminRepairList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("管理员报修列表: status={}, page={}", status, page);
        return Result.success(repairAppService.getAdminRepairList(status, page, size));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新报修状态（管理员）")
    public Result<Map<String, Object>> updateRepairStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, Object> command) {
        log.info("更新报修状态: id={}, status={}", id, command.get("status"));
        return Result.success(repairAppService.updateRepairStatus(id, command));
    }
}
