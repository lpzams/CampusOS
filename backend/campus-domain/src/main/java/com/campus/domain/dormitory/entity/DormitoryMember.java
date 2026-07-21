package com.campus.domain.dormitory.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DormitoryMember {
    private Long id;
    private String dormitoryId;
    private Long userId;
    private Integer isRoomLeader;
    private LocalDateTime createTime;
}
