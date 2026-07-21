package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("t_course_enrollment")
public class CourseEnrollmentPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("course_id") private Long courseId;
    @TableField("student_id") private Long studentId;
    private String semester;
    @TableField("create_time") private LocalDateTime createTime;
}
