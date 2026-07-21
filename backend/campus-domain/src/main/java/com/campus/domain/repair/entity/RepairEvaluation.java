package com.campus.domain.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairEvaluation {
    private Long id;
    private Long repairId;
    private Long userId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;

    public static RepairEvaluation create(Long repairId, Long userId, Integer score, String content) {
        RepairEvaluation e = new RepairEvaluation();
        e.setRepairId(repairId);
        e.setUserId(userId);
        e.setScore(score);
        e.setContent(content);
        e.setCreateTime(LocalDateTime.now());
        return e;
    }
}
