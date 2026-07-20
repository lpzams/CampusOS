package com.campus.domain.exam.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRoom {

    private Long id;
    private Long examId;
    private String building;
    private String classroom;
    private Integer capacity;
    private LocalDateTime createTime;

    // ========== 工厂方法 ==========

    public static ExamRoom create(Long examId, String building, String classroom, Integer capacity) {
        ExamRoom room = new ExamRoom();
        room.setExamId(examId);
        room.setBuilding(building);
        room.setClassroom(classroom);
        room.setCapacity(capacity != null ? capacity : 60);
        room.setCreateTime(LocalDateTime.now());
        return room;
    }
}
