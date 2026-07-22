package com.campus.application.course;

import com.campus.common.exception.BusinessException;
import com.campus.domain.course.teaching.CrawlerTeachingRepository;
import com.campus.domain.course.teaching.TeachingCatalogLinkRepository;
import com.campus.domain.shared.CampusRecord;
import com.campus.domain.shared.CampusRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Links teaching courses, student enrollment, feedback and grades through a single course id. */
@Service
public class TeachingAppService {
    private final CampusRecordRepository records;
    private final CrawlerTeachingRepository crawlerTeaching;
    private final TeachingCatalogLinkRepository catalogLinks;

    public TeachingAppService(CampusRecordRepository records, CrawlerTeachingRepository crawlerTeaching, TeachingCatalogLinkRepository catalogLinks) {
        this.records = records;
        this.crawlerTeaching = crawlerTeaching;
        this.catalogLinks = catalogLinks;
    }

    public List<Map<String, Object>> catalog() {
        List<Map<String, Object>> courses = list("teachingCourse");
        Map<Long, Map<String, Object>> links = catalogLinks.findLinks(courses.stream().map(c -> longValue(c.get("id"))).toList());
        return courses.stream().map(course -> {
            Map<String, Object> row = new LinkedHashMap<>(course);
            Map<String, Object> link = links.get(longValue(course.get("id")));
            row.put("sourceId", String.valueOf(course.get("id"))); // formal offering id used by enrollment
            if (link != null) row.putAll(link);
            row.putIfAbsent("professor", course.get("teacherName"));
            row.putIfAbsent("reviewCount", 0);
            return row;
        }).toList();
    }
    public List<Map<String, Object>> mine(Long studentId) { return enrollmentRows(studentId); }

    @Transactional public void enroll(Long studentId, String sourceId) {
        Long courseId = parseId(sourceId);
        Map<String, Object> selectedCourse = course(courseId);
        if (records.findByType("courseEnrollment").stream().anyMatch(r -> same(r.getData(), "studentId", studentId) && same(r.getData(), "courseId", courseId)))
            throw new BusinessException(400, "已选择该课程");
        boolean conflicts = records.findByType("courseEnrollment").stream()
                .filter(r -> same(r.getData(), "studentId", studentId))
                .map(r -> course(longValue(r.getData().get("courseId"))))
                .anyMatch(existing -> same(existing, "semester", selectedCourse.get("semester"))
                        && same(existing, "dayOfWeek", selectedCourse.get("dayOfWeek"))
                        && same(existing, "timeSlot", selectedCourse.get("timeSlot")));
        if (conflicts) throw new BusinessException(400, "该时间段已有已选课程，请选择其他课程");
        save("courseEnrollment", Map.of("studentId", studentId, "courseId", courseId, "selectedAt", LocalDateTime.now().toString()));
    }

    @Transactional public void cancel(Long studentId, String sourceId) {
        Long courseId = parseId(sourceId);
        CampusRecord enrollment = records.findByType("courseEnrollment").stream()
                .filter(r -> same(r.getData(), "studentId", studentId) && same(r.getData(), "courseId", courseId)).findFirst()
                .orElseThrow(() -> new BusinessException(404, "未找到选课记录"));
        records.delete("courseEnrollment", enrollment.getId());
    }

    public List<Map<String, Object>> teacherCourses(Long teacherId) { return list("teachingCourse").stream().filter(c -> same(c, "teacherId", teacherId)).toList(); }
    public List<Map<String, Object>> allCourses() { return catalog(); }
    public List<Map<String, Object>> teachers() { return list("user").stream().filter(user -> same(user, "userType", 2)).map(user -> Map.of("userId", user.get("id"), "realName", user.getOrDefault("realName", user.get("username")), "department", user.getOrDefault("department", ""))).toList(); }

    @Transactional public Map<String, Object> arrange(Map<String, Object> body) {
        Long teacherId = longValue(body.get("teacherId"));
        Map<String, Object> teacher = user(teacherId);
        if (!same(teacher, "userType", 2)) throw new BusinessException(400, "只能安排教师承担课程");
        String courseName = required(body, "courseName"), timeSlot = required(body, "timeSlot"), classroom = required(body, "classroom");
        int day = intValue(body.get("dayOfWeek"));
        if (day < 1 || day > 7) throw new BusinessException(400, "上课日期必须为 1 到 7");
        String semester = String.valueOf(body.getOrDefault("semester", "2026-2027-1"));
        ensureNoConflict(null, teacherId, semester, day, timeSlot, classroom);
        Map<String, Object> linkedCatalog = requestedCatalog(body);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teacherId", teacherId); data.put("teacherName", teacher.getOrDefault("realName", teacher.get("username")));
        data.put("courseName", linkedCatalog == null ? courseName : linkedCatalog.get("courseName")); data.put("courseCode", body.getOrDefault("courseCode", "")); data.put("credit", body.getOrDefault("credit", 2));
        data.put("semester", semester); data.put("dayOfWeek", day); data.put("timeSlot", timeSlot);
        data.put("classroom", classroom); data.put("building", body.getOrDefault("building", "")); data.put("weeks", body.getOrDefault("weeks", "1-16周")); data.put("color", body.getOrDefault("color", "#409EFF"));
        CampusRecord saved = save("teachingCourse", data);
        if (linkedCatalog != null) catalogLinks.link(saved.getId(), String.valueOf(linkedCatalog.get("sourceId")));
        return view(saved);
    }

    @Transactional public Map<String, Object> reschedule(Long courseId, Map<String, Object> body) {
        CampusRecord record = records.findById("teachingCourse", courseId).orElseThrow(() -> new BusinessException(404, "教学课程不存在"));
        Long teacherId = body.containsKey("teacherId") ? longValue(body.get("teacherId")) : longValue(record.getData().get("teacherId"));
        Map<String, Object> teacher = user(teacherId);
        if (!same(teacher, "userType", 2)) throw new BusinessException(400, "只能安排教师承担课程");
        int day = body.containsKey("dayOfWeek") ? intValue(body.get("dayOfWeek")) : intValue(record.getData().get("dayOfWeek"));
        String semester = String.valueOf(body.getOrDefault("semester", record.getData().get("semester")));
        String time = String.valueOf(body.getOrDefault("timeSlot", record.getData().get("timeSlot"))), room = String.valueOf(body.getOrDefault("classroom", record.getData().get("classroom")));
        ensureNoConflict(courseId, teacherId, semester, day, time, room);
        Map<String, Object> linkedCatalog = requestedCatalog(body);
        record.getData().putAll(body); record.getData().put("teacherId", teacherId); record.getData().put("teacherName", teacher.getOrDefault("realName", teacher.get("username")));
        if (linkedCatalog != null) record.getData().put("courseName", linkedCatalog.get("courseName"));
        CampusRecord saved = records.save(record);
        if (body.containsKey("catalogSourceId")) {
            if (linkedCatalog == null) catalogLinks.unlink(courseId); else catalogLinks.link(courseId, String.valueOf(linkedCatalog.get("sourceId")));
        }
        return view(saved);
    }

    @Transactional public void removeCourse(Long courseId) {
        boolean enrolled = records.findByType("courseEnrollment").stream().anyMatch(row -> same(row.getData(), "courseId", courseId));
        if (enrolled) throw new BusinessException(400, "已有学生选课，不能删除；请先调整课程安排");
        catalogLinks.unlink(courseId);
        records.delete("teachingCourse", courseId);
    }

    /** Courses mapped from the public review catalog, separate from official scheduled teaching courses. */
    public List<Map<String, Object>> catalogTeacherCourses(Long teacherId) {
        return crawlerTeaching.findCoursesByTeacherId(teacherId);
    }

    public List<Map<String, Object>> catalogStudents(Long teacherId, String courseSourceId) {
        if (courseSourceId == null || courseSourceId.isBlank()) throw new BusinessException(400, "课程标识不能为空");
        if (!crawlerTeaching.ownsCourse(teacherId, courseSourceId)) throw new BusinessException(403, "只能查看已关联到本人的课程");
        return crawlerTeaching.findStudents(teacherId, courseSourceId);
    }

    public List<Map<String, Object>> students(Long teacherId, Long courseId) {
        Map<String, Object> course = ownedCourse(teacherId, courseId);
        return records.findByType("courseEnrollment").stream().filter(r -> same(r.getData(), "courseId", courseId)).map(r -> {
            Long studentId = longValue(r.getData().get("studentId"));
            Map<String, Object> user = user(studentId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", studentId); row.put("username", user.get("username")); row.put("realName", user.get("realName"));
            row.put("courseId", courseId); row.put("courseName", course.get("courseName")); row.put("score", score(studentId, courseId));
            return row;
        }).toList();
    }

    public List<Map<String, Object>> reviews(Long teacherId, Long courseId) {
        ownedCourse(teacherId, courseId);
        return list("courseFeedback").stream().filter(r -> same(r, "courseId", courseId)).toList();
    }

    @Transactional public Map<String, Object> grade(Long teacherId, Long courseId, Long studentId, Number value) {
        Map<String, Object> course = ownedCourse(teacherId, courseId);
        if (value == null || value.doubleValue() < 0 || value.doubleValue() > 100) throw new BusinessException(400, "成绩必须在 0 到 100 之间");
        boolean enrolled = records.findByType("courseEnrollment").stream().anyMatch(r -> same(r.getData(), "courseId", courseId) && same(r.getData(), "studentId", studentId));
        if (!enrolled) throw new BusinessException(403, "该学生未报名此课程");
        CampusRecord existing = records.findByType("score").stream().filter(r -> same(r.getData(), "courseId", courseId) && same(r.getData(), "userId", studentId)).findFirst().orElse(null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", studentId); data.put("courseId", courseId); data.put("courseName", course.get("courseName"));
        data.put("courseCode", course.get("courseCode")); data.put("credit", course.getOrDefault("credit", 2));
        data.put("semester", course.get("semester")); data.put("type", "课程成绩"); data.put("score", value); data.put("grade", gradeName(value.doubleValue()));
        data.put("teacherId", teacherId); data.put("updatedTime", LocalDateTime.now().toString());
        if (existing == null) return view(save("score", data));
        existing.getData().putAll(data); return view(records.save(existing));
    }

    private List<Map<String, Object>> enrollmentRows(Long studentId) {
        return records.findByType("courseEnrollment").stream().filter(r -> same(r.getData(), "studentId", studentId)).map(r -> {
            Map<String, Object> course = course(longValue(r.getData().get("courseId")));
            Map<String, Object> row = new LinkedHashMap<>(course); row.put("sourceId", String.valueOf(course.get("id"))); row.put("selectedAt", r.getData().get("selectedAt")); return row;
        }).toList();
    }

    private Map<String, Object> ownedCourse(Long teacherId, Long courseId) { Map<String, Object> c = course(courseId); if (!same(c, "teacherId", teacherId)) throw new BusinessException(403, "只能操作自己教学的课程"); return c; }
    private Map<String, Object> course(Long id) { return view(records.findById("teachingCourse", id).orElseThrow(() -> new BusinessException(404, "教学课程不存在"))); }
    private Map<String, Object> user(Long id) { return view(records.findById("user", id).orElseThrow(() -> new BusinessException(404, "学生不存在"))); }
    private Object score(Long studentId, Long courseId) { return records.findByType("score").stream().filter(r -> same(r.getData(), "userId", studentId) && same(r.getData(), "courseId", courseId)).findFirst().map(r -> r.getData().get("score")).orElse(null); }
    private List<Map<String, Object>> list(String type) { return records.findByType(type).stream().map(this::view).toList(); }
    private CampusRecord save(String type, Map<String, Object> data) { return records.save(new CampusRecord(null, type, data)); }
    private Map<String, Object> view(CampusRecord r) { Map<String, Object> result = new LinkedHashMap<>(); result.put("id", r.getId()); result.putAll(r.getData()); return result; }
    private boolean same(Map<String, Object> row, String key, Object value) { return String.valueOf(row.get(key)).equals(String.valueOf(value)); }
    private Long parseId(String id) { try { return Long.valueOf(id); } catch (Exception e) { throw new BusinessException(400, "课程标识无效"); } }
    private Long longValue(Object value) { return Long.valueOf(String.valueOf(value)); }
    private String gradeName(double score) { return score >= 90 ? "优秀" : score >= 80 ? "良好" : score >= 60 ? "及格" : "不及格"; }
    private String required(Map<String, Object> body, String key) { Object value = body.get(key); if (value == null || value.toString().isBlank()) throw new BusinessException(400, key + " 不能为空"); return value.toString().trim(); }
    private int intValue(Object value) { try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { throw new BusinessException(400, "数值格式错误"); } }
    private Map<String, Object> requestedCatalog(Map<String, Object> body) {
        if (!body.containsKey("catalogSourceId")) return null;
        Object value = body.get("catalogSourceId");
        if (value == null || value.toString().isBlank()) return null;
        Map<String, Object> catalog = catalogLinks.findCatalogCourse(value.toString().trim());
        if (catalog == null) throw new BusinessException(400, "关联的爬虫课程不存在");
        return catalog;
    }
    private void ensureNoConflict(Long excludedCourseId, Long teacherId, String semester, int day, String time, String classroom) {
        for (Map<String, Object> existing : list("teachingCourse")) {
            if (excludedCourseId != null && excludedCourseId.equals(longValue(existing.get("id")))) continue;
            if (semester.equals(String.valueOf(existing.get("semester")))
                    && intValue(existing.get("dayOfWeek")) == day && time.equals(String.valueOf(existing.get("timeSlot")))) {
                if (same(existing, "teacherId", teacherId)) throw new BusinessException(400, "该教师在此时间已有授课安排");
                if (classroom.equals(String.valueOf(existing.get("classroom")))) throw new BusinessException(400, "该教室在此时间已被占用");
            }
        }
    }
}
