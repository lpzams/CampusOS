package com.campus.domain.exam.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamSeat {

    private Long id;
    private Long examId;
    private Long roomId;
    private Integer seatNumber;
    private Long studentUserId;
    private LocalDateTime createTime;

    // ========== 工厂方法 ==========

    public static ExamSeat create(Long examId, Long roomId, Integer seatNumber, Long studentUserId) {
        ExamSeat seat = new ExamSeat();
        seat.setExamId(examId);
        seat.setRoomId(roomId);
        seat.setSeatNumber(seatNumber);
        seat.setStudentUserId(studentUserId);
        seat.setCreateTime(LocalDateTime.now());
        return seat;
    }
}
