package com.campus.application.course.service;

import com.campus.application.course.command.ClassroomCommand;
import com.campus.application.course.command.UpdateClassroomCommand;
import com.campus.application.course.dto.ClassroomListDTO;
import com.campus.application.course.dto.FreeClassroomDTO;
import com.campus.application.course.dto.FreeClassroomResponseDTO;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.course.entity.Classroom;
import com.campus.domain.course.entity.ClassroomStatus;
import com.campus.domain.course.entity.ClassroomType;
import com.campus.domain.course.repository.ClassroomRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class ClassroomAdminService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public Map<String, Object> addClassroom(ClassroomCommand cmd) {
        requireAdmin();
        int type = cmd.getType() != null ? cmd.getType() : ClassroomType.NORMAL;
        if (!ClassroomType.isValid(type)) throw new BusinessException("教室类型不合法，有效值：1-普通 2-多媒体 3-智慧 4-实验室 5-机房");

        Classroom room = new Classroom();
        room.setBuilding(cmd.getBuilding());
        room.setName(cmd.getName());
        room.setFloor(cmd.getFloor() != null ? cmd.getFloor() : 1);
        room.setCapacity(cmd.getCapacity() != null ? cmd.getCapacity() : 60);
        room.setType(type);
        room.setStatus(ClassroomStatus.FREE); // 默认空闲
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());
        classroomRepository.save(room);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", room.getId()); r.put("building", room.getBuilding());
        r.put("name", room.getName()); r.put("capacity", room.getCapacity());
        r.put("createTime", room.getCreateTime().format(FMT));
        return r;
    }

    public PageResult<ClassroomListDTO> getClassroomList(String building, int page, int size) {
        requireAdmin();
        List<Classroom> rooms = classroomRepository.findPage(building, (page - 1) * size, size);
        long total = classroomRepository.count(building);
        List<ClassroomListDTO> list = rooms.stream().map(r -> ClassroomListDTO.builder()
                .id(r.getId()).building(r.getBuilding()).name(r.getName())
                .floor(r.getFloor()).capacity(r.getCapacity())
                .type(r.getType()).typeName(r.getTypeName())
                .status(r.getStatusName()).build()).collect(Collectors.toList());
        return PageResult.of(total, list, page, size);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        requireAdmin();
        if (classroomRepository.findById(id) == null) throw new BusinessException("教室不存在");
        classroomRepository.delete(id);
    }

    @Transactional
    public Map<String, Object> updateClassroom(Long id, UpdateClassroomCommand cmd) {
        requireAdmin();
        Classroom room = classroomRepository.findById(id);
        if (room == null) throw new BusinessException("教室不存在");

        if (cmd.getBuilding() != null) room.setBuilding(cmd.getBuilding());
        if (cmd.getName() != null) room.setName(cmd.getName());
        if (cmd.getFloor() != null) room.setFloor(cmd.getFloor());
        if (cmd.getCapacity() != null) room.setCapacity(cmd.getCapacity());
        if (cmd.getType() != null) {
            if (!ClassroomType.isValid(cmd.getType())) throw new BusinessException("教室类型不合法");
            room.setType(cmd.getType());
        }
        room.setUpdateTime(LocalDateTime.now());
        classroomRepository.update(room);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", room.getId()); r.put("building", room.getBuilding());
        r.put("name", room.getName()); r.put("capacity", room.getCapacity());
        r.put("type", room.getType()); r.put("typeName", room.getTypeName());
        r.put("updateTime", room.getUpdateTime().format(FMT));
        return r;
    }

    public List<Map<String, Object>> getAllClassrooms() {
        List<Classroom> rooms = classroomRepository.findAll(null);
        return rooms.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId()); m.put("building", r.getBuilding());
            m.put("name", r.getName()); m.put("capacity", r.getCapacity());
            return m;
        }).collect(Collectors.toList());
    }

    // ==================== 5.B5 空闲教室（公开，已从排课模块迁移至教室模块） ====================

    public FreeClassroomResponseDTO getFreeClassrooms(String building, String dateStr, String timeSlot) {
        LocalDate date = (dateStr != null && !dateStr.isEmpty())
                ? LocalDate.parse(dateStr, DATE_FMT) : LocalDate.now();
        int dow = date.getDayOfWeek().getValue();
        if (timeSlot == null || timeSlot.isEmpty()) timeSlot = "08:00-09:35";

        if (dow > 5) {
            return FreeClassroomResponseDTO.builder()
                    .date(date.format(DATE_FMT)).timeSlot(timeSlot).building(building)
                    .freeClassrooms(Collections.emptyList()).build();
        }

        List<Classroom> allRooms = classroomRepository.findAll(building).stream()
                .filter(Classroom::isAvailable).collect(Collectors.toList());
        List<Long> occupiedIds = classroomRepository.findOccupiedIds(dow, timeSlot);
        Set<Long> occupied = new HashSet<>(occupiedIds);

        List<FreeClassroomDTO> freeList = allRooms.stream()
                .filter(r -> !occupied.contains(r.getId()))
                .map(r -> FreeClassroomDTO.builder()
                        .id(r.getId()).classroom(r.getName()).building(r.getBuilding())
                        .floor(r.getFloor()).capacity(r.getCapacity())
                        .type(r.getTypeName()).status("空闲").build())
                .collect(Collectors.toList());

        return FreeClassroomResponseDTO.builder()
                .date(date.format(DATE_FMT)).timeSlot(timeSlot).building(building)
                .freeClassrooms(freeList).build();
    }

    private void requireAdmin() {
        Long uid = UserAppService.getCurrentUserId();
        User u = userRepository.findById(uid);
        if (u == null || !u.isAdmin()) throw new BusinessException(ResultCode.PERMISSION_DENIED);
    }
}
