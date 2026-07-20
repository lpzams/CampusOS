package com.campus.application.score.service;

import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.score.entity.Course;
import com.campus.domain.score.repository.CourseRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAdminService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 5.5 创建课程（管理员） ====================

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCourse(Map<String, Object> command) {
        // 1. 校验管理员权限
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 2. 查教师姓名
        Long teacherId = command.get("teacherId") != null ? ((Number) command.get("teacherId")).longValue() : null;
        String teacherName = null;
        if (teacherId != null) {
            User teacher = userRepository.findById(teacherId);
            teacherName = teacher != null ? teacher.getRealName() : null;
        }

        // 3. 创建课程
        Course course = Course.create(
                (String) command.get("name"),
                (String) command.get("courseCode"),
                command.get("credit") != null ? ((Number) command.get("credit")).intValue() : null,
                teacherId,
                teacherName,
                (String) command.get("semester"),
                command.get("maxStudents") != null ? ((Number) command.get("maxStudents")).intValue() : null,
                (String) command.get("color")
        );

        courseRepository.save(course);

        log.info("课程创建成功: id={}, name={}", course.getId(), course.getName());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", course.getId());
        response.put("name", course.getName());
        response.put("courseCode", course.getCourseCode());
        response.put("createTime", course.getCreateTime() != null ? course.getCreateTime().format(FORMATTER) : null);
        return response;
    }

    // ==================== 5.6 更新课程（管理员） ====================

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateCourse(Long id, Map<String, Object> command) {
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        Course course = courseRepository.findById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        course.updateFields(
                (String) command.get("name"),
                command.get("credit") != null ? ((Number) command.get("credit")).intValue() : null,
                command.get("maxStudents") != null ? ((Number) command.get("maxStudents")).intValue() : null
        );
        courseRepository.update(course);

        log.info("课程更新成功: id={}", id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", course.getId());
        response.put("name", course.getName());
        response.put("updateTime", course.getUpdateTime() != null ? course.getUpdateTime().format(FORMATTER) : null);
        return response;
    }

    // ==================== 5.7 删除课程（管理员） ====================

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        Course course = courseRepository.findById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        courseRepository.delete(id);
        log.info("课程删除成功: id={}", id);
    }

    // ==================== 5.9 获取课程列表（管理员） ====================

    public PageResult<Map<String, Object>> getCourseList(String semester, String keyword, int page, int size) {
        List<Course> courses = courseRepository.findPage(semester, keyword, (page - 1) * size, size);
        long total = courseRepository.count(semester, keyword);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Course course : courses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", course.getId());
            item.put("name", course.getName());
            item.put("courseCode", course.getCourseCode());
            item.put("credit", course.getCredit());
            item.put("teacherName", course.getTeacherName());
            item.put("semester", course.getSemester());
            item.put("maxStudents", course.getMaxStudents());
            item.put("color", course.getColor());
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }
}
