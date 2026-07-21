package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("t_classroom")
public class ClassroomPO {
    @TableId(type = IdType.AUTO) private Long id;
    private String building;
    private String name;
    private Integer floor;
    private Integer capacity;
    private Integer type;
    private Integer status;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}
