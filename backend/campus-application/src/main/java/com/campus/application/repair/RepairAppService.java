package com.campus.application.repair;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.common.exception.BusinessException;
import com.campus.domain.repair.RepairOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepairAppService {
    private final CampusAppService records;
    public RepairAppService(CampusAppService records) { this.records = records; }

    @Transactional
    public Map<String, Object> submit(Long userId, Map<String, Object> command) {
        for (String key : List.of("type", "title", "description", "building", "room", "contactPhone")) Values.required(command, key);
        Map<String, Object> data = new LinkedHashMap<>(command);
        data.put("status", "待处理");
        return records.create("repair", Values.owned(userId, data));
    }

    public List<Map<String, Object>> list(Long userId) { return records.listExact("repair", "userId", userId); }
    public List<Map<String, Object>> all() { return records.list("repair"); }

    @Transactional
    public Map<String, Object> progress(Long id, String status, String statusDesc) {
        if (!List.of("处理中", "已完成").contains(status)) throw Values.invalid(new IllegalArgumentException("不支持的工单状态"));
        records.get("repair", id);
        return records.update("repair", id, Map.of("status", status, "statusDesc", statusDesc == null ? "" : statusDesc.trim()));
    }

    public Map<String, Object> detail(Long userId, Long id) {
        Map<String, Object> row = records.get("repair", id);
        if (row.get("userId") != null && !userId.toString().equals(String.valueOf(row.get("userId"))))
            throw new BusinessException(403, "无权查看该报修单");
        return row;
    }

    @Transactional
    public Map<String, Object> evaluate(Long userId, Map<String, Object> command) {
        Long id = Values.longValue(command.get("repairId"));
        Map<String, Object> row = detail(userId, id);
        RepairOrder order = new RepairOrder(parseStatus(row.get("status")));
        try { order.evaluate(Values.intValue(command.get("score")), Values.required(command, "content")); }
        catch (RuntimeException e) { throw Values.invalid(e); }
        return records.update("repair", id, Map.of("status", "已评价", "score", order.score(), "evaluation", order.evaluation()));
    }

    private RepairOrder.Status parseStatus(Object status) {
        return switch (String.valueOf(status)) {
            case "处理中" -> RepairOrder.Status.PROCESSING;
            case "已完成" -> RepairOrder.Status.COMPLETED;
            case "已评价" -> RepairOrder.Status.EVALUATED;
            default -> RepairOrder.Status.SUBMITTED;
        };
    }
}
