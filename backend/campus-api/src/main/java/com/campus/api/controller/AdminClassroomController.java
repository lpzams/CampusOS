package com.campus.api.controller;

import com.campus.application.course.command.ClassroomCommand;
import com.campus.application.course.command.UpdateClassroomCommand;
import com.campus.application.course.dto.ClassroomListDTO;
import com.campus.application.course.service.ClassroomAdminService;
import com.campus.common.api.PageResult;
import com.campus.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j @RestController @RequestMapping("/api/admin/classroom") @RequiredArgsConstructor
@Tag(name = "教室管理(管理员)", description = "教室CRUD")
public class AdminClassroomController {

    private final ClassroomAdminService classroomAdminService;

    @PostMapping
    @Operation(summary = "5.A1 添加教室")
    public Result<Map<String, Object>> add(@Valid @RequestBody ClassroomCommand cmd) {
        return Result.success(classroomAdminService.addClassroom(cmd));
    }

    @GetMapping("/list")
    @Operation(summary = "5.A2 教室列表")
    public Result<PageResult<ClassroomListDTO>> list(@RequestParam(required = false) String building,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(classroomAdminService.getClassroomList(building, page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "5.A3 删除教室")
    public Result<Void> delete(@PathVariable Long id) {
        classroomAdminService.deleteClassroom(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "5.A4 更新教室")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody UpdateClassroomCommand cmd) {
        return Result.success(classroomAdminService.updateClassroom(id, cmd));
    }
}
