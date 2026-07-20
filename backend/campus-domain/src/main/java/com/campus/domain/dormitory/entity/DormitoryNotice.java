package com.campus.domain.dormitory.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DormitoryNotice {
    private Long id;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createTime;
}
