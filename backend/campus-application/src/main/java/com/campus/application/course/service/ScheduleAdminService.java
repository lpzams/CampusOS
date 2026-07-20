package com.campus.application.course.service;

import com.campus.application.course.command.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.course.entity.*;
import com.campus.domain.course.repository.*;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import com.campus.application.course.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleAdminService {

    private final ScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 5.B1 排课 ====================

    @Transactional
    public Map<String, Object> schedule(ScheduleCourseCommand cmd) {
        requireAdmin();
        Course course = courseRepository.findById(cmd.getCourseId());
        if (course == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        Classroom room = classroomRepository.findById(cmd.getClassroomId());
        if (room == null) throw new BusinessException("教室不存在");

        Schedule s = new Schedule();
        s.setCourseId(cmd.getCourseId());
        s.setClassroomId(cmd.getClassroomId());
        s.setDayOfWeek(cmd.getDayOfWeek());
        s.setTimeSlot(cmd.getTimeSlot());
        s.setWeeks(cmd.getWeeks());
        s.setCreateTime(LocalDateTime.now());
        scheduleRepository.save(s);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", s.getId()); r.put("courseId", course.getId());
        r.put("courseName", course.getName()); r.put("classroomId", room.getId());
        r.put("classroom", room.getName()); r.put("dayOfWeek", s.getDayOfWeek());
        r.put("timeSlot", s.getTimeSlot()); r.put("weeks", s.getWeeks());
        r.put("createTime", s.getCreateTime().format(FMT));
        return r;
    }

    // ==================== 5.B2 批量排课 ====================

    @Transactional
    public Map<String, Integer> batchSchedule(BatchScheduleCommand cmd) {
        requireAdmin();
        int success = 0, fail = 0;
        for (ScheduleCourseCommand sc : cmd.getSchedules()) {
            try { schedule(sc); success++; } catch (Exception e) { fail++; }
        }
        Map<String, Integer> r = new LinkedHashMap<>();
        r.put("successCount", success);
        r.put("failCount", fail);
        return r;
    }

    // ==================== 5.B3 排课列表 ====================

    public PageResult<Map<String, Object>> getScheduleList(String semester, Long teacherId, int page, int size) {
        requireAdmin();
        List<Schedule> list = scheduleRepository.findPage(semester, teacherId, (page - 1) * size, size);
        long total = scheduleRepository.count(semester, teacherId);
        List<Map<String, Object>> items = list.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId()); m.put("courseId", s.getCourseId());
            m.put("courseName", s.getCourseName()); m.put("teacherId", s.getTeacherId());
            m.put("teacherName", s.getTeacherName()); m.put("classroomId", s.getClassroomId());
            m.put("classroom", s.getClassroom()); m.put("building", s.getBuilding());
            m.put("dayOfWeek", s.getDayOfWeek()); m.put("dayName", s.getDayName());
            m.put("timeSlot", s.getTimeSlot()); m.put("weeks", s.getWeeks());
            return m;
        }).collect(Collectors.toList());
        return PageResult.of(total, items, page, size);
    }

    // ==================== 5.B4 取消排课 ====================

    @Transactional
    public void cancelSchedule(Long id) {
        requireAdmin();
        if (scheduleRepository.findById(id) == null) throw new BusinessException("排课记录不存在");
        scheduleRepository.delete(id);
    }

    // ==================== 5.B5 空闲教室 ====================

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

        // 只查正常状态的教室
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
