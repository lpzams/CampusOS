package com.campus.api.controller;

import com.campus.application.location.service.LocationAppService;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Tag(name = "校园地图导航", description = "地点列表、详情、搜索、路径规划相关接口")
public class LocationController {

    private final LocationAppService locationAppService;

    // ==================== 14.1 获取校园地点列表 ====================

    @GetMapping("/list")
    @Operation(summary = "获取校园地点列表")
    public Result<List<Map<String, Object>>> getLocationList(
            @RequestParam(required = false) String category) {
        log.info("获取地点列表: category={}", category);
        return Result.success(locationAppService.getLocationList(category));
    }

    // ==================== 14.2 获取地点详情 ====================

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取地点详情")
    public Result<Map<String, Object>> getLocationDetail(@PathVariable Long id) {
        log.info("获取地点详情: id={}", id);
        return Result.success(locationAppService.getLocationDetail(id));
    }

    // ==================== 14.3 搜索地点 ====================

    @GetMapping("/search")
    @Operation(summary = "搜索地点")
    public Result<List<Map<String, Object>>> searchLocations(
            @RequestParam String keyword,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double lat) {
        log.info("搜索地点: keyword={}, lng={}, lat={}", keyword, lng, lat);
        return Result.success(locationAppService.searchLocations(keyword, lng, lat));
    }

    // ==================== 14.4 获取路径规划 ====================

    @PostMapping("/route")
    @Operation(summary = "获取路径规划")
    public Result<Map<String, Object>> getRoute(@RequestBody Map<String, Object> command) {
        Double fromLng = ((Number) command.get("fromLng")).doubleValue();
        Double fromLat = ((Number) command.get("fromLat")).doubleValue();
        Double toLng = ((Number) command.get("toLng")).doubleValue();
        Double toLat = ((Number) command.get("toLat")).doubleValue();
        String mode = (String) command.getOrDefault("mode", "walk");
        log.info("路径规划: from=({},{}), to=({},{}), mode={}", fromLng, fromLat, toLng, toLat, mode);
        return Result.success(locationAppService.getRoute(fromLng, fromLat, toLng, toLat, mode));
    }
}
