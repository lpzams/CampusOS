package com.campus.domain.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairProgress {
    private Long id;
    private Long repairId;
    private String status;
    private String content;
    private LocalDateTime createTime;

    public static RepairProgress create(Long repairId, String status, String content) {
        RepairProgress p = new RepairProgress();
        p.setRepairId(repairId);
        p.setStatus(status);
        p.setContent(content);
        p.setCreateTime(LocalDateTime.now());
        return p;
    }
}
