package com.campus.application.dormitory;

import com.campus.application.shared.CampusAppService;
import com.campus.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DormitoryAppService {
    private final CampusAppService records;
    public DormitoryAppService(CampusAppService records) { this.records = records; }

    public Map<String, Object> info(Long userId) {
        List<Map<String, Object>> rows = records.list("dormitory");
        return rows.stream().filter(row -> userId.toString().equals(String.valueOf(row.get("userId")))).findFirst()
                .or(() -> rows.stream().filter(row -> row.get("userId") == null).findFirst())
                .orElseThrow(() -> new BusinessException(404, "宿舍信息不存在"));
    }
    public List<Map<String, Object>> notices() { return records.list("dormitoryNotice"); }
    public Map<String, Object> utility(Long userId) {
        Map<String, Object> row = info(userId);
        return Map.of("electricityBalance", row.getOrDefault("electricityBalance", 0),
                "waterBalance", row.getOrDefault("waterBalance", 0));
    }
}
