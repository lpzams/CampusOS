package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_exam_room")
public class ExamRoomPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("exam_id")
    private Long examId;

    private String building;
    private String classroom;
    private Integer capacity;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
