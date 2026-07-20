package com.campus.application.course.service;

import com.campus.application.course.command.*;
import com.campus.application.course.dto.CourseListDTO;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.course.entity.*;
import com.campus.domain.course.repository.*;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAdminService {

    private final CourseRepository courseRepository;
    private final ScheduleRepository scheduleRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 5.4 创建课程 ====================

    @Transactional
    public Map<String, Object> createCourse(CreateCourseCommand cmd) {
        requireAdmin();
        if (courseRepository.existsByCourseCode(cmd.getCourseCode()))
            throw new BusinessException("课程编码已存在");

        // 解析timeSlot: "周一 08:00-09:35" → dayOfWeek + timeSlot
        int[] parsed = parseTimeSlot(cmd.getTimeSlot());
        String timePart = cmd.getTimeSlot().replaceFirst("^周[一二三四五六日]\\s*", "");

        // 查找教室
        Classroom room = classroomRepository.findById(
                findOrGetClassroomId(cmd.getBuilding(), cmd.getClassroom()));

        User teacher = userRepository.findById(cmd.getTeacherId());
        if (teacher == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);

        // 创建课程
        Course course = new Course();
        course.setName(cmd.getName());
        course.setCourseCode(cmd.getCourseCode());
        course.setCredit(cmd.getCredit());
        course.setTeacherId(cmd.getTeacherId());
        course.setTeacherName(teacher.getRealName());
        course.setSemester(cmd.getSemester());
        course.setMaxStudents(cmd.getMaxStudents() != null ? cmd.getMaxStudents() : 60);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        courseRepository.save(course);

        // 创建排课
        Schedule schedule = new Schedule();
        schedule.setCourseId(course.getId());
        schedule.setClassroomId(room.getId());
        schedule.setDayOfWeek(parsed[0]);
        schedule.setTimeSlot(timePart);
        schedule.setWeeks(cmd.getWeeks());
        schedule.setCreateTime(LocalDateTime.now());
        scheduleRepository.save(schedule);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", course.getId());
        r.put("name", course.getName());
        r.put("courseCode", course.getCourseCode());
        r.put("createTime", course.getCreateTime().format(FMT));
        return r;
    }

    // ==================== 5.5 更新课程 ====================

    @Transactional
    public Map<String, Object> updateCourse(Long id, UpdateCourseCommand cmd) {
        requireAdmin();
        Course course = courseRepository.findById(id);
        if (course == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);

        if (cmd.getCourseCode() != null && !cmd.getCourseCode().equals(course.getCourseCode())) {
            if (courseRepository.existsByCourseCodeExcludingId(cmd.getCourseCode(), id))
                throw new BusinessException("课程编码已存在");
            course.setCourseCode(cmd.getCourseCode());
        }
        if (cmd.getName() != null) course.setName(cmd.getName());
        if (cmd.getCredit() != null) course.setCredit(cmd.getCredit());
        if (cmd.getTeacherId() != null) {
            course.setTeacherId(cmd.getTeacherId());
            User t = userRepository.findById(cmd.getTeacherId());
            if (t != null) course.setTeacherName(t.getRealName());
        }
        if (cmd.getSemester() != null) course.setSemester(cmd.getSemester());
        if (cmd.getMaxStudents() != null) course.setMaxStudents(cmd.getMaxStudents());
        course.setUpdateTime(LocalDateTime.now());
        courseRepository.update(course);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", course.getId());
        r.put("name", course.getName());
        r.put("updateTime", course.getUpdateTime().format(FMT));
        return r;
    }

    // ==================== 5.6 删除课程 ====================

    @Transactional
    public void deleteCourse(Long id) {
        requireAdmin();
        if (courseRepository.findById(id) == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        scheduleRepository.deleteByCourseId(id);
        courseRepository.delete(id);
    }

    // ==================== 5.7 分配教师 ====================

    @Transactional
    public Map<String, Object> assignTeacher(AssignTeacherCommand cmd) {
        requireAdmin();
        Course course = courseRepository.findById(cmd.getCourseId());
        if (course == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        User teacher = userRepository.findById(cmd.getTeacherId());
        if (teacher == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);

        course.setTeacherId(cmd.getTeacherId());
        course.setTeacherName(teacher.getRealName());
        course.setUpdateTime(LocalDateTime.now());
        courseRepository.update(course);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("courseId", course.getId());
        r.put("teacherId", teacher.getId());
        r.put("teacherName", teacher.getRealName());
        return r;
    }

    // ==================== 5.8 课程列表 ====================

    public PageResult<CourseListDTO> getCourseList(String semester, String keyword, int page, int size) {
        requireAdmin();
        List<Course> courses = courseRepository.findPage(semester, keyword, (page - 1) * size, size);
        long total = courseRepository.count(semester, keyword);
        List<CourseListDTO> list = courses.stream().map(c -> {
            Schedule s = scheduleRepository.findByCourseId(c.getId());
            return CourseListDTO.builder()
                    .id(c.getId()).name(c.getName()).courseCode(c.getCourseCode())
                    .credit(c.getCredit()).teacherName(c.getTeacherName()).semester(c.getSemester())
                    .classroom(s != null ? s.getClassroom() : null)
                    .studentCount((int) enrollmentRepository.countByCourseId(c.getId()))
                    .maxStudents(c.getMaxStudents()).build();
        }).collect(java.util.stream.Collectors.toList());
        return PageResult.of(total, list, page, size);
    }

    // ==================== helpers ====================

    private void requireAdmin() {
        Long uid = UserAppService.getCurrentUserId();
        User u = userRepository.findById(uid);
        if (u == null || !u.isAdmin()) throw new BusinessException(ResultCode.PERMISSION_DENIED);
    }

    /** 解析 "周一 08:00-09:35" → [dayOfWeek, ...] */
    static int[] parseTimeSlot(String ts) {
        String[] map = {"", "周一","周二","周三","周四","周五","周六","周日"};
        for (int i = 1; i <= 7; i++) {
            if (ts.startsWith(map[i])) return new int[]{i};
        }
        return new int[]{1}; // fallback
    }

    private Long findOrGetClassroomId(String building, String name) {
        Classroom r = classroomRepository.findByBuildingAndName(building, name);
        if (r != null) return r.getId();
        // 自动创建教室
        Classroom nr = new Classroom();
        nr.setBuilding(building);
        nr.setName(name);
        nr.setFloor(1);
        nr.setCapacity(60);
        nr.setType(1); // 默认普通教室
        nr.setStatus(0);
        nr.setCreateTime(LocalDateTime.now());
        nr.setUpdateTime(LocalDateTime.now());
        classroomRepository.save(nr);
        return nr.getId();
    }
}
