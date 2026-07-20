package com.campus.application.course.service;

import com.campus.application.course.command.SelectCourseCommand;
import com.campus.application.course.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.course.entity.*;
import com.campus.domain.course.repository.*;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAppService {

    private final CourseRepository courseRepository;
    private final ScheduleRepository scheduleRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 5.1 获取个人课表 ====================

    public ScheduleResponseDTO getSchedule(Long userId, String semester, Integer week) {
        User user = requireUser(userId);
        if (!user.isStudent()) throw new BusinessException("仅学生可查看课表");
        if (semester == null || semester.isEmpty()) semester = getCurrentSemester();
        if (week == null) week = getCurrentWeek();

        List<Schedule> schedules = scheduleRepository.findByStudentAndSemester(userId, semester);
        final int fw = week;
        schedules = schedules.stream().filter(s -> s.isActiveInWeek(fw)).collect(Collectors.toList());

        // 构建课表：5天×5时段
        Map<String, List<Schedule>> grouped = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getDayOfWeek() + "|" + s.getTimeSlot(),
                        LinkedHashMap::new, Collectors.toList()));

        List<ScheduleSlotDTO> slots = new ArrayList<>();
        for (int d = 1; d <= 5; d++) {
            for (String ts : new String[]{"08:00-09:35","10:00-11:35","14:00-15:35","16:00-17:35","19:00-20:35"}) {
                List<Schedule> sc = grouped.getOrDefault(d + "|" + ts, Collections.emptyList());
                slots.add(ScheduleSlotDTO.builder()
                        .dayOfWeek(d).dayName(Course.DAY_NAMES[d]).timeSlot(ts)
                        .courses(sc.stream().map(this::toItem).collect(Collectors.toList())).build());
            }
        }
        UserProfile p = userProfileRepository.findByUserId(userId);
        return ScheduleResponseDTO.builder().semester(semester).week(week)
                .studentId(p != null ? p.getStudentId() : null).schedule(slots).build();
    }

    // ==================== 5.2 获取今日课程 ====================

    public TodayCourseResponseDTO getTodayCourses(Long userId) {
        User user = requireUser(userId);
        if (!user.isStudent()) throw new BusinessException("仅学生可查看今日课程");
        LocalDate today = LocalDate.now();
        int dow = today.getDayOfWeek().getValue();
        String semester = getCurrentSemester();
        int week = getCurrentWeek();
        if (dow > 5) return TodayCourseResponseDTO.builder().date(today.format(DATE_FMT))
                .dayOfWeek(dow).week(week).semester(semester).courses(Collections.emptyList()).build();

        List<Schedule> schedules = scheduleRepository.findByStudentAndSemester(userId, semester);
        final int fw = week;
        LocalTime now = LocalTime.now();
        List<CourseItemDTO> items = schedules.stream()
                .filter(s -> s.getDayOfWeek() != null && s.getDayOfWeek() == dow && s.isActiveInWeek(fw))
                .map(s -> toItemWithStatus(s, now)).collect(Collectors.toList());
        return TodayCourseResponseDTO.builder().date(today.format(DATE_FMT))
                .dayOfWeek(dow).week(week).semester(semester).courses(items).build();
    }

    // ==================== 5.3 获取教师授课列表 ====================

    public com.campus.application.course.dto.TeacherCourseResponseDTO getTeacherCourses(Long teacherId, String semester) {
        User teacher = requireUser(teacherId);
        if (semester == null || semester.isEmpty()) semester = getCurrentSemester();
        List<Course> courses = courseRepository.findByTeacherAndSemester(teacherId, semester);

        List<TeacherCourseDTO> list = courses.stream().map(c -> {
            Schedule s = scheduleRepository.findByCourseId(c.getId());
            return TeacherCourseDTO.builder()
                    .id(c.getId()).name(c.getName()).courseCode(c.getCourseCode()).credit(c.getCredit())
                    .classroom(s != null ? s.getClassroom() : null)
                    .timeSlot(s != null ? s.getDayName() + " " + s.getTimeSlot() : null)
                    .students((int) enrollmentRepository.countByCourseId(c.getId()))
                    .schedule(s != null ? s.getWeeks() : null).build();
        }).collect(Collectors.toList());

        log.info("教师授课列表: teacherId={}, count={}", teacherId, list.size());
        return com.campus.application.course.dto.TeacherCourseResponseDTO.builder()
                .teacherId(teacherId).teacherName(teacher.getRealName())
                .department(teacher.getDepartment()).semester(semester).courses(list).build();
    }

    // ==================== 5.9 获取我的课程（教师） ====================

    public List<com.campus.application.course.dto.CourseListDTO> getMyCourses(Long userId) {
        User user = requireUser(userId);
        if (!user.isTeacher()) throw new BusinessException("仅教师可查看");
        String semester = getCurrentSemester();
        List<Course> courses = courseRepository.findByTeacherAndSemester(userId, semester);
        return courses.stream().map(c -> {
            Schedule s = scheduleRepository.findByCourseId(c.getId());
            return com.campus.application.course.dto.CourseListDTO.builder()
                    .id(c.getId()).name(c.getName()).courseCode(c.getCourseCode())
                    .credit(c.getCredit()).teacherName(c.getTeacherName()).semester(c.getSemester())
                    .classroom(s != null ? s.getClassroom() : null)
                    .timeSlot(s != null ? s.getDayName() + " " + s.getTimeSlot() : null)
                    .studentCount((int) enrollmentRepository.countByCourseId(c.getId()))
                    .maxStudents(c.getMaxStudents()).build();
        }).collect(Collectors.toList());
    }

    // ==================== 5.C1 选课 ====================

    @Transactional
    public Map<String, Object> selectCourse(Long studentId, SelectCourseCommand cmd) {
        Course course = courseRepository.findById(cmd.getCourseId());
        if (course == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);

        // 检查人数
        long current = enrollmentRepository.countByCourseId(cmd.getCourseId());
        if (current >= course.getMaxStudents()) throw new BusinessException("选课人数已满");
        if (enrollmentRepository.existsByCourseAndStudent(cmd.getCourseId(), studentId))
            throw new BusinessException("已选过该课程");

        CourseEnrollment e = CourseEnrollment.create(cmd.getCourseId(), studentId, course.getSemester());
        enrollmentRepository.save(e);

        Map<String, Object> result = new HashMap<>();
        result.put("selectionId", e.getId());
        result.put("courseId", course.getId());
        result.put("courseName", course.getName());
        result.put("selectedTime", e.getCreateTime().format(FMT));
        return result;
    }

    // ==================== 5.C2 退课 ====================

    @Transactional
    public void dropCourse(Long studentId, Long courseId) {
        if (!enrollmentRepository.existsByCourseAndStudent(courseId, studentId))
            throw new BusinessException("未选该课程");
        enrollmentRepository.delete(courseId, studentId);
    }

    // ==================== 5.C3 可选课程列表 ====================

    public PageResult<CourseListDTO> getAvailableCourses(Long studentId, String semester, String keyword, int page, int size) {
        if (semester == null || semester.isEmpty()) semester = getCurrentSemester();
        List<Course> courses = courseRepository.findAvailable(semester, keyword, (page - 1) * size, size);
        long total = courseRepository.countAvailable(semester, keyword);

        List<CourseListDTO> list = courses.stream().map(c -> {
            Schedule s = scheduleRepository.findByCourseId(c.getId());
            return CourseListDTO.builder()
                    .id(c.getId()).name(c.getName()).courseCode(c.getCourseCode()).credit(c.getCredit())
                    .teacherName(c.getTeacherName()).semester(c.getSemester())
                    .classroom(s != null ? s.getClassroom() : null)
                    .timeSlot(s != null ? s.getDayName() + " " + s.getTimeSlot() : null)
                    .currentStudents((int) enrollmentRepository.countByCourseId(c.getId()))
                    .maxStudents(c.getMaxStudents())
                    .isSelected(enrollmentRepository.existsByCourseAndStudent(c.getId(), studentId))
                    .build();
        }).collect(Collectors.toList());
        return PageResult.of(total, list, page, size);
    }

    // ==================== 5.C4 我的选课列表 ====================

    public List<Map<String, Object>> getMySelections(Long studentId, String semester) {
        if (semester == null || semester.isEmpty()) semester = getCurrentSemester();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudentAndSemester(studentId, semester);
        return enrollments.stream().map(e -> {
            Course c = courseRepository.findById(e.getCourseId());
            Schedule s = c != null ? scheduleRepository.findByCourseId(c.getId()) : null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("courseId", e.getCourseId());
            m.put("courseName", c != null ? c.getName() : null);
            m.put("courseCode", c != null ? c.getCourseCode() : null);
            m.put("credit", c != null ? c.getCredit() : null);
            m.put("teacherName", c != null ? c.getTeacherName() : null);
            m.put("classroom", s != null ? s.getClassroom() : null);
            m.put("timeSlot", s != null ? s.getDayName() + " " + s.getTimeSlot() : null);
            m.put("selectedTime", e.getCreateTime() != null ? e.getCreateTime().format(FMT) : null);
            m.put("status", "已选");
            return m;
        }).collect(Collectors.toList());
    }

    // ==================== 5.C5 课程学生名单 ====================

    public StudentSelectionDTO getCourseStudents(Long courseId) {
        Course course = courseRepository.findById(courseId);
        if (course == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        List<Long> studentIds = enrollmentRepository.findStudentIdsByCourseId(courseId);
        List<StudentItemDTO> students = new ArrayList<>();
        for (Long sid : studentIds) {
            User u = userRepository.findById(sid);
            UserProfile p = userProfileRepository.findByUserId(sid);
            if (u != null) students.add(StudentItemDTO.builder()
                    .userId(u.getId()).studentId(p != null ? p.getStudentId() : null)
                    .realName(u.getRealName()).department(u.getDepartment())
                    .major(p != null ? p.getMajor() : null)
                    .className(p != null ? p.getClassName() : null).build());
        }
        return StudentSelectionDTO.builder().courseId(courseId).courseName(course.getName())
                .totalStudents(students.size()).list(students).build();
    }

    // ==================== 5.C6 选课统计 ====================

    public CourseStatisticsDTO getCourseStatistics(Long courseId) {
        Course course = courseRepository.findById(courseId);
        if (course == null) throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        List<Long> studentIds = enrollmentRepository.findStudentIdsByCourseId(courseId);
        int total = studentIds.size();

        Map<String, Integer> deptMap = new LinkedHashMap<>();
        Map<String, Integer> classMap = new LinkedHashMap<>();
        for (Long sid : studentIds) {
            User u = userRepository.findById(sid);
            UserProfile p = userProfileRepository.findByUserId(sid);
            if (u != null) {
                deptMap.merge(u.getDepartment(), 1, Integer::sum);
                if (p != null && p.getClassName() != null)
                    classMap.merge(p.getClassName(), 1, Integer::sum);
            }
        }
        return CourseStatisticsDTO.builder()
                .courseId(courseId).courseName(course.getName())
                .totalStudents(total).maxStudents(course.getMaxStudents())
                .remainingSlots(course.getMaxStudents() - total)
                .byDepartment(deptMap.entrySet().stream().map(e ->
                        CourseStatisticsDTO.DepartmentCount.builder().department(e.getKey()).count(e.getValue()).build())
                        .collect(Collectors.toList()))
                .byClass(classMap.entrySet().stream().map(e ->
                        CourseStatisticsDTO.ClassCount.builder().className(e.getKey()).count(e.getValue()).build())
                        .collect(Collectors.toList()))
                .build();
    }

    // ==================== helpers ====================

    private CourseItemDTO toItem(Schedule s) {
        return CourseItemDTO.builder().id(s.getCourseId()).name(s.getCourseName())
                .teacher(s.getTeacherName()).classroom(s.getClassroom()).building(s.getBuilding())
                .timeSlot(s.getTimeSlot()).weeks(s.getWeeks()).build();
    }

    private CourseItemDTO toItemWithStatus(Schedule s, LocalTime now) {
        CourseItemDTO b = toItem(s);
        return CourseItemDTO.builder().id(b.getId()).name(b.getName()).teacher(b.getTeacher())
                .classroom(b.getClassroom()).building(b.getBuilding()).timeSlot(b.getTimeSlot())
                .weeks(b.getWeeks()).color(b.getColor()).credit(b.getCredit())
                .status(calcStatus(s.getTimeSlot(), now)).build();
    }

    private String calcStatus(String ts, LocalTime now) {
        try {
            String[] p = ts.split("-");
            LocalTime st = LocalTime.parse(p[0].trim()), ed = LocalTime.parse(p[1].trim());
            if (now.isBefore(st)) return "未开始";
            if (now.isAfter(ed)) return "已结束";
            return "进行中";
        } catch (Exception e) { return "未知"; }
    }

    private User requireUser(Long id) {
        User u = userRepository.findById(id);
        if (u == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);
        return u;
    }

    public static String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        int m = now.getMonthValue(), y = now.getYear();
        return (m >= 9 || m <= 1) ? y + "-" + (y + 1) + "-1" : (y - 1) + "-" + y + "-2";
    }

    public static int getCurrentWeek() {
        LocalDate now = LocalDate.now();
        LocalDate start = (now.getMonthValue() >= 9 || now.getMonthValue() <= 1)
                ? LocalDate.of(now.getMonthValue() >= 9 ? now.getYear() : now.getYear() - 1, 9, 1)
                : LocalDate.of(now.getYear(), 2, 20);
        return Math.max(1, (int) (java.time.temporal.ChronoUnit.DAYS.between(start, now) / 7) + 1);
    }
}
