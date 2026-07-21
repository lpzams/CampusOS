package com.campus.infrastructure.persistence.repair;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

// ===== POs =====
@Data @TableName("t_repair")
class RepairPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("user_id") private Long userId;
    private String type; @TableField("type_code") private String typeCode;
    private String title; private String description;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String images;
    private String building; private String room; @TableField("contact_phone") private String contactPhone;
    private String status; @TableField("status_desc") private String statusDesc;
    @TableField("expected_time") private LocalDateTime expectedTime;
    @TableField("complete_time") private LocalDateTime completeTime;
    private String handler; @TableField("handler_phone") private String handlerPhone;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}

@Data @TableName("t_repair_progress")
class RepairProgressPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("repair_id") private Long repairId;
    private String status; private String content;
    @TableField("create_time") private LocalDateTime createTime;
}

@Data @TableName("t_repair_evaluation")
class RepairEvaluationPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("repair_id") private Long repairId;
    @TableField("user_id") private Long userId;
    private Integer score; private String content;
    @TableField("create_time") private LocalDateTime createTime;
}

// ===== Mappers =====
@Mapper interface RepairMapper extends BaseMapper<RepairPO> {
    @Select("SELECT * FROM t_repair WHERE deleted = 0 AND user_id = #{userId} ORDER BY create_time DESC")
    List<RepairPO> selectByUserId(@Param("userId") Long userId);

    @Select("<script>SELECT * FROM t_repair WHERE deleted = 0 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}</script>")
    List<RepairPO> selectPage(@Param("status") String status, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_repair WHERE deleted = 0 " +
            "<if test='status != null'> AND status = #{status} </if></script>")
    long count(@Param("status") String status);
}

@Mapper interface RepairProgressMapper extends BaseMapper<RepairProgressPO> {
    @Select("SELECT * FROM t_repair_progress WHERE repair_id = #{repairId} ORDER BY create_time ASC")
    List<RepairProgressPO> selectByRepairId(@Param("repairId") Long repairId);
}

@Mapper interface RepairEvaluationMapper extends BaseMapper<RepairEvaluationPO> {
    @Select("SELECT * FROM t_repair_evaluation WHERE repair_id = #{repairId}")
    RepairEvaluationPO selectByRepairId(@Param("repairId") Long repairId);
}
