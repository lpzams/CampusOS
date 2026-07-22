package com.campus.domain.shared;

import java.util.LinkedHashMap;
import java.util.Map;

public class CampusRecord {
    private Long id;
    private String type;
    private Map<String, Object> data = new LinkedHashMap<>();

    public CampusRecord() {}

    public CampusRecord(Long id, String type, Map<String, Object> data) {
        this.id = id;
        this.type = type;
        this.data = new LinkedHashMap<>(data);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
