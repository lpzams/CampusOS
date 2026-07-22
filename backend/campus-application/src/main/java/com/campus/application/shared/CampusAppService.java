package com.campus.application.shared;

import com.campus.common.api.PageResult;
import com.campus.common.exception.BusinessException;
import com.campus.domain.shared.CampusRecord;
import com.campus.domain.shared.CampusRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CampusAppService {
    private final CampusRecordRepository repository;

    public CampusAppService(CampusRecordRepository repository) { this.repository = repository; }

    public List<Map<String, Object>> list(String type) {
        return repository.findByType(type).stream().map(this::view).toList();
    }

    public List<Map<String, Object>> search(String type, String key, Object value) {
        if (value == null || value.toString().isBlank()) return list(type);
        String expected = value.toString().toLowerCase();
        // ponytail: Java-side filtering is enough for demo data; add typed tables/indexes when volume requires it.
        return list(type).stream().filter(row -> String.valueOf(row.get(key)).toLowerCase().contains(expected)).toList();
    }

    public List<Map<String, Object>> listExact(String type, String key, Object value) {
        return list(type).stream().filter(row -> java.util.Objects.equals(String.valueOf(row.get(key)), String.valueOf(value))).toList();
    }

    public PageResult<Map<String, Object>> page(String type, int page, int size) {
        List<Map<String, Object>> all = list(type);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(safePage, safeSize, all.size(), all.subList(from, to));
    }

    public Map<String, Object> get(String type, Long id) {
        return view(repository.findById(type, id)
                .orElseThrow(() -> new BusinessException("资源不存在，id=" + id)));
    }

    @Transactional
    public Map<String, Object> create(String type, Map<String, Object> data) {
        Map<String, Object> copy = new LinkedHashMap<>(data);
        copy.putIfAbsent("createTime", LocalDateTime.now().toString());
        return view(repository.save(new CampusRecord(null, type, copy)));
    }

    @Transactional
    public Map<String, Object> update(String type, Long id, Map<String, Object> changes) {
        CampusRecord record = repository.findById(type, id)
                .orElseThrow(() -> new BusinessException("资源不存在，id=" + id));
        record.getData().putAll(changes);
        record.getData().put("updateTime", LocalDateTime.now().toString());
        return view(repository.save(record));
    }

    @Transactional
    public void delete(String type, Long id) { repository.delete(type, id); }

    private Map<String, Object> view(CampusRecord record) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.getId());
        result.putAll(record.getData());
        return result;
    }
}
