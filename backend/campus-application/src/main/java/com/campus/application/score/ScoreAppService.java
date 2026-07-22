package com.campus.application.score;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.domain.score.ScorePolicy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreAppService {
    private final CampusAppService records;
    public ScoreAppService(CampusAppService records) { this.records = records; }

    public Map<String, Object> list(Long userId, String semester, String type) {
        List<Map<String, Object>> rows = filtered(userId, semester, type);
        Map<String, Object> result = statistics(rows);
        result.put("semester", semester);
        result.put("scores", rows);
        return result;
    }

    public Map<String, Object> gpa(Long userId, String semester) {
        ScorePolicy.Statistics value = calculate(filtered(userId, semester, null));
        return Map.of("gpa", value.gpa(), "totalCredits", value.totalCredits());
    }

    public Map<String, Object> statistics(Long userId, String semester) {
        return statistics(filtered(userId, semester, null));
    }

    private List<Map<String, Object>> filtered(Long userId, String semester, String type) {
        return records.list("score").stream()
                .filter(s -> userId.toString().equals(String.valueOf(s.get("userId"))))
                .filter(s -> semester == null || semester.isBlank() || s.get("semester") == null || semester.equals(s.get("semester")))
                .filter(s -> type == null || type.isBlank() || type.equals(s.get("type"))).toList();
    }

    private Map<String, Object> statistics(List<Map<String, Object>> rows) {
        ScorePolicy.Statistics value = calculate(rows);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gpa", value.gpa()); result.put("totalCredits", value.totalCredits());
        result.put("average", value.average()); result.put("count", value.count());
        return result;
    }

    private ScorePolicy.Statistics calculate(List<Map<String, Object>> rows) {
        return ScorePolicy.calculate(rows.stream().map(row -> new ScorePolicy.CourseScore(
                Values.decimal(row.getOrDefault("credit", 0)), Values.decimal(row.getOrDefault("score", 0)))).toList());
    }
}
