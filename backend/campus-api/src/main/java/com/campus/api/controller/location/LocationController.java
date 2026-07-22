package com.campus.api.controller.location;

import com.campus.application.location.LocationAppService; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/location")
public class LocationController {
    private final LocationAppService app; public LocationController(LocationAppService app){this.app=app;}
    @GetMapping("/list") public Result<List<Map<String,Object>>> list(@RequestParam(required=false) String category){return Result.success(app.list(category));}
    @GetMapping("/detail/{id}") public Result<Map<String,Object>> detail(@PathVariable Long id){return Result.success(app.detail(id));}
    @GetMapping("/search") public Result<List<Map<String,Object>>> search(@RequestParam String keyword){return Result.success(app.search(keyword));}
    @PostMapping("/route") public Result<Map<String,Object>> route(@RequestBody Map<String,Object> body){return Result.success(app.route(body));}
}
