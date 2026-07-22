package com.campus.api;

import com.campus.domain.auth.CredentialService;
import com.campus.domain.shared.CampusRecord;
import com.campus.domain.shared.CampusRecordRepository;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 校园门户系统 —— 后端启动类。
 *
 * <p>【DDD 洋葱架构·api 层（最外层）】
 * <ul>
 *   <li>这里是整个后端唯一的启动入口，也是 Spring 扫描的根。</li>
 *   <li>{@code @SpringBootApplication} 默认扫描 com.campus 下所有 @Component/@Service/@Repository，
 *       所以 application / infrastructure 层的 Spring Bean 都能被扫到。</li>
 *   <li>{@code @MapperScan} 单独指定 MyBatis-Plus 的 Mapper 接口所在包。</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.campus")
@MapperScan("com.campus.infrastructure.persistence")
public class CampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAdmin(CampusRecordRepository users, CredentialService credentials) {
        return args -> {
            seedUser(users, credentials, "admin", "管理员", "13800138000", "admin@campus.local", 3, "ROLE_ADMIN", "信息化办公室", null);
            seedUser(users, credentials, "student", "演示学生", "13800138001", "student@campus.local", 1, "ROLE_STUDENT", "计算机学院", "20260001");
            seedUser(users, credentials, "teacher", "演示教师", "13800138002", "teacher@campus.local", 2, "ROLE_TEACHER", "计算机学院", "T20260001");
            seedTeachingData(users);
        };
    }

    private void seedUser(CampusRecordRepository users, CredentialService credentials, String username, String realName,
            String phone, String email, int userType, String roleCode, String department, String studentId) {
        if (users.findByType("user").stream().anyMatch(user -> username.equals(user.getData().get("username")))) return;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("passwordHash", credentials.hash("123456"));
        data.put("realName", realName);
        data.put("phone", phone);
        data.put("email", email);
        data.put("userType", userType);
        data.put("roleCode", roleCode);
        data.put("department", department);
        if (studentId != null) data.put("studentId", studentId);
        data.put("status", 0);
        users.save(new CampusRecord(null, "user", data));
    }

    private void seedTeachingData(CampusRecordRepository users) {
        if (!users.findByType("teachingCourse").isEmpty()) return;
        Long teacherId = users.findByType("user").stream().filter(user -> "teacher".equals(user.getData().get("username"))).findFirst().orElseThrow().getId();
        Long studentId = users.findByType("user").stream().filter(user -> "student".equals(user.getData().get("username"))).findFirst().orElseThrow().getId();
        CampusRecord java = users.save(new CampusRecord(null, "teachingCourse", teachingCourse(teacherId, "演示教师", "Java 程序设计", "CS201", 1, "08:00-09:35", "A101", "教学楼 A", "#409EFF")));
        CampusRecord data = users.save(new CampusRecord(null, "teachingCourse", teachingCourse(teacherId, "演示教师", "数据结构", "CS202", 3, "14:30-16:05", "A305", "教学楼 A", "#67C23A")));
        users.save(new CampusRecord(null, "courseEnrollment", Map.of("studentId", studentId, "courseId", java.getId(), "selectedAt", "2026-07-21T09:00:00")));
        users.save(new CampusRecord(null, "courseEnrollment", Map.of("studentId", studentId, "courseId", data.getId(), "selectedAt", "2026-07-21T09:00:00")));
        users.save(new CampusRecord(null, "courseFeedback", Map.of("courseId", java.getId(), "studentId", studentId, "overall", 5, "comment", "课堂节奏清晰，实验反馈及时。", "createdTime", "2026-07-21T10:00:00")));
    }

    private Map<String, Object> teachingCourse(Long teacherId, String teacherName, String courseName, String courseCode,
            int dayOfWeek, String timeSlot, String classroom, String building, String color) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teacherId", teacherId); data.put("teacherName", teacherName); data.put("courseName", courseName); data.put("courseCode", courseCode);
        data.put("semester", "2026-2027-1"); data.put("credit", 3); data.put("dayOfWeek", dayOfWeek); data.put("timeSlot", timeSlot);
        data.put("classroom", classroom); data.put("building", building); data.put("weeks", "1-16周"); data.put("color", color);
        return data;
    }
}
