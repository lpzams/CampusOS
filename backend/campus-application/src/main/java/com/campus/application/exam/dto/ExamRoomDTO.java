package com.campus.application.exam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 考场安排响应（7.7）
 */
@Data
@Builder
public class ExamRoomDTO {

    private Long examId;
    private String courseName;
    private String examDate;
    private String examTime;
    private List<RoomInfo> rooms;

    @Data
    @Builder
    public static class RoomInfo {
        private Long roomId;
        private String building;
        private String classroom;
        private Integer capacity;
        private Integer studentCount;
        private List<SeatInfo> seats;
    }

    @Data
    @Builder
    public static class SeatInfo {
        private Integer seatNumber;
        private String studentId;
        private String realName;
    }
}
