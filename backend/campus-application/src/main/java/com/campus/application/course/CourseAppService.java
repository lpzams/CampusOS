package com.campus.application.course;

import com.campus.application.shared.CampusAppService;
import com.campus.domain.shared.CampusRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class CourseAppService {
    private final CampusAppService records;
    private final CampusRecordRepository repository;
    public CourseAppService(CampusAppService records, CampusRecordRepository repository) { this.records = records; this.repository = repository; }

    public Map<String, Object> schedule(Long userId, Integer userType, String semester, int week) {
        return Map.of("semester", semester, "week", Math.max(1, week), "schedule", visible(userId, userType));
    }

    public List<Map<String, Object>> today(Long userId, Integer userType) {
        int day = LocalDate.now().getDayOfWeek().getValue();
        return visible(userId, userType).stream().filter(row -> day == Integer.parseInt(String.valueOf(row.getOrDefault("dayOfWeek", 0)))).toList();
    }

    public List<Map<String, Object>> freeClassrooms(String building, String date, String timeSlot) {
        return records.list("classroom").stream()
                .filter(c -> building == null || building.isBlank() || building.equals(c.get("building")))
                .filter(c -> timeSlot == null || timeSlot.isBlank() || timeSlot.equals(c.get("timeSlot")))
                .filter(c -> !Boolean.FALSE.equals(c.get("available"))).toList();
    }

    public List<Map<String, Object>> teacherCourses(Long teacherId) {
        return records.listExact("course", "teacherId", teacherId);
    }

    private List<Map<String, Object>> visible(Long userId, Integer userType) {
        if (userType != null && (userType == 1 || userType == 2)) {
            List<Map<String, Object>> teaching = records.list("teachingCourse");
            if (userType == 2) return toSlots(teaching.stream().filter(course -> userId.toString().equals(String.valueOf(course.get("teacherId")))).toList());
            java.util.Set<String> selected = repository.findByType("courseEnrollment").stream()
                    .filter(row -> userId.toString().equals(String.valueOf(row.getData().get("studentId"))))
                    .map(row -> String.valueOf(row.getData().get("courseId"))).collect(java.util.stream.Collectors.toSet());
            return toSlots(teaching.stream().filter(course -> selected.contains(String.valueOf(course.get("id")))).toList());
        }
        return records.list("course").stream().filter(row -> row.get("userId") == null || userId.toString().equals(String.valueOf(row.get("userId")))).toList();
    }

    private List<Map<String, Object>> toSlots(List<Map<String, Object>> courses) {
        return courses.stream().map(course -> {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", course.get("id")); info.put("name", course.get("courseName")); info.put("teacher", course.get("teacherName"));
            info.put("classroom", course.get("classroom")); info.put("building", course.get("building")); info.put("weeks", course.getOrDefault("weeks", "1-16周")); info.put("color", course.getOrDefault("color", "#409EFF"));
            Map<String, Object> slot = new LinkedHashMap<>(); slot.put("dayOfWeek", course.get("dayOfWeek")); slot.put("timeSlot", course.get("timeSlot")); slot.put("courses", List.of(info)); return slot;
        }).toList();
    }
}
