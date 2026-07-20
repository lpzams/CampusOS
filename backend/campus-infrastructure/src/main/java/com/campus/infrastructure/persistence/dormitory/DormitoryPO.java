package com.campus.infrastructure.persistence.dormitory;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("t_dormitory")
public class DormitoryPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("dormitory_id") private String dormitoryId;
    private String building; private String room; private String type;
    @TableField("type_name") private String typeName;
    private String facilities;
    @TableField("create_time") private LocalDateTime createTime;
}

@Data @TableName("t_dormitory_member")
class DormitoryMemberPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("dormitory_id") private String dormitoryId;
    @TableField("user_id") private Long userId;
    @TableField("is_room_leader") private Integer isRoomLeader;
    @TableField("create_time") private LocalDateTime createTime;
}

@Data @TableName("t_dormitory_notice")
class DormitoryNoticePO {
    @TableId(type = IdType.AUTO) private Long id;
    private String title; private String content; private String type;
    @TableField("create_time") private LocalDateTime createTime;
    @TableLogic private Integer deleted;
}

@Data @TableName("t_dormitory_utility")
class DormitoryUtilityPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("dormitory_id") private String dormitoryId;
    @TableField("electricity_balance") private java.math.BigDecimal electricityBalance;
    @TableField("water_balance") private java.math.BigDecimal waterBalance;
    @TableField("update_time") private LocalDateTime updateTime;
}

@Data @TableName("t_dormitory_utility_record")
class DormitoryUtilityRecordPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("dormitory_id") private String dormitoryId;
    @TableField("record_month") private String recordMonth;
    @TableField("electricity_consumption") private java.math.BigDecimal electricityConsumption;
    @TableField("electricity_cost") private java.math.BigDecimal electricityCost;
    @TableField("water_consumption") private java.math.BigDecimal waterConsumption;
    @TableField("water_cost") private java.math.BigDecimal waterCost;
    @TableField("create_time") private LocalDateTime createTime;
}
