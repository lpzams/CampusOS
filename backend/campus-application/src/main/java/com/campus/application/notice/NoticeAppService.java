package com.campus.application.notice;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.application.notice.command.PublishNoticeCommand;
import com.campus.common.api.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class NoticeAppService {
    private final CampusAppService records;

    public NoticeAppService(CampusAppService records) { this.records = records; }

    public PageResult<Map<String, Object>> page(String type, String department, int page, int size) {
        List<Map<String, Object>> filtered = records.list("notice").stream()
                .filter(n -> type == null || type.isBlank() || type.equals(n.get("type")))
                .filter(n -> department == null || department.isBlank() || department.equals(n.get("department")))
                .toList();
        int safePage = Math.max(1, page), safeSize = Math.min(100, Math.max(1, size));
        int from = Math.min((safePage - 1) * safeSize, filtered.size());
        return new PageResult<>(safePage, safeSize, filtered.size(),
                filtered.subList(from, Math.min(from + safeSize, filtered.size())));
    }

    public Map<String, Object> detail(Long id) { return records.get("notice", id); }

    @Transactional
    public Long publish(PublishNoticeCommand command) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", command.getTitle().trim()); data.put("content", command.getContent().trim());
        data.put("type", command.getType()); data.put("department", command.getDepartment().trim());
        return Values.longValue(records.create("notice", data).get("id"));
    }

    @Transactional
    public void markRead(Long userId, Long noticeId) {
        if (records.listExact("noticeRead", "userId", userId).stream()
                .noneMatch(row -> noticeId.equals(Values.longValue(row.get("noticeId")))))
            records.create("noticeRead", Values.owned(userId, Map.of("noticeId", noticeId)));
    }

    public long unreadCount(Long userId) {
        long read = records.listExact("noticeRead", "userId", userId).stream()
                .map(row -> Values.longValue(row.get("noticeId"))).distinct().count();
        return Math.max(0, records.list("notice").size() - read);
    }
}
