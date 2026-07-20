package com.campus.domain.dormitory.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Dormitory {
    private Long id;
    private String dormitoryId;
    private String building;
    private String room;
    private String type;
    private String typeName;
    private String facilities;
    private LocalDateTime createTime;
}
