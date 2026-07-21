package com.campus.infrastructure.persistence.exam;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_exam_seat")
public class ExamSeatPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("exam_id")
    private Long examId;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_number")
    private Integer seatNumber;

    @TableField("student_user_id")
    private Long studentUserId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
