package com.campus.application.exam;

import com.campus.application.shared.CampusAppService;
import com.campus.application.exam.command.PublishExamCommand;
import com.campus.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class ExamAppService {
    private final CampusAppService records;
    public ExamAppService(CampusAppService records) { this.records = records; }

    public List<Map<String, Object>> list(Long userId) { return visible(userId); }

    public Long publish(PublishExamCommand command) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseName", command.getCourseName().trim()); data.put("courseCode", command.getCourseCode());
        data.put("examDate", command.getExamDate()); data.put("examTime", command.getExamTime().trim());
        data.put("building", command.getBuilding().trim()); data.put("classroom", command.getClassroom().trim());
        data.put("seatNumber", command.getSeatNumber()); data.put("status", "待考试");
        if (command.getUserId() != null) data.put("userId", command.getUserId());
        return com.campus.application.shared.Values.longValue(records.create("exam", data).get("id"));
    }

    public List<Map<String, Object>> calendar(Long userId, String month) {
        try { YearMonth.parse(month); }
        catch (DateTimeParseException e) { throw new BusinessException(400, "月份格式应为 YYYY-MM"); }
        return visible(userId).stream().filter(row -> String.valueOf(row.get("examDate")).startsWith(month)).toList();
    }

    private List<Map<String, Object>> visible(Long userId) {
        return records.list("exam").stream().filter(row -> row.get("userId") == null || userId.toString().equals(String.valueOf(row.get("userId")))).toList();
    }
}
