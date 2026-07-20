package com.campus.infrastructure.persistence.activity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

// ===== POs =====

@Data @TableName("t_activity")
class ActivityPO {
    @TableId(type = IdType.AUTO) private Long id;
    private String title;
    private String category;
    @TableField("cover_image") private String coverImage;
    private String content;
    @TableField("start_time") private LocalDateTime startTime;
    @TableField("end_time") private LocalDateTime endTime;
    private String location;
    @TableField("max_participants") private Integer maxParticipants;
    @TableField("current_participants") private Integer currentParticipants;
    private String organizer;
    @TableField("contact_phone") private String contactPhone;
    private String status;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}

@Data @TableName("t_activity_registration")
class ActivityRegistrationPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("activity_id") private Long activityId;
    @TableField("user_id") private Long userId;
    private String status;
    @TableField("checkin_status") private Integer checkinStatus;
    @TableField("checkin_code") private String checkinCode;
    @TableField("checkin_time") private LocalDateTime checkinTime;
    private String remark;
    @TableField("register_time") private LocalDateTime registerTime;
}

// ===== Mappers =====

@Mapper
interface ActivityMapper extends BaseMapper<ActivityPO> {

    @Select("<script>SELECT * FROM t_activity WHERE deleted = 0 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}</script>")
    List<ActivityPO> selectPage(@Param("category") String category,
                                @Param("status") String status,
                                @Param("offset") int offset,
                                @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_activity WHERE deleted = 0 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if></script>")
    long count(@Param("category") String category, @Param("status") String status);

    @Select("<script>SELECT * FROM t_activity WHERE deleted = 0 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "ORDER BY id DESC LIMIT #{offset}, #{size}</script>")
    List<ActivityPO> selectAdminPage(@Param("category") String category,
                                     @Param("status") String status,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_activity WHERE deleted = 0 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if></script>")
    long countAdmin(@Param("category") String category, @Param("status") String status);

    @Update("UPDATE t_activity SET current_participants = current_participants + 1 WHERE id = #{id} AND current_participants < max_participants")
    int incrementParticipants(@Param("id") Long id);

    @Update("UPDATE t_activity SET current_participants = GREATEST(current_participants - 1, 0) WHERE id = #{id}")
    int decrementParticipants(@Param("id") Long id);
}

@Mapper
interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistrationPO> {

    @Select("SELECT * FROM t_activity_registration WHERE activity_id = #{activityId} AND user_id = #{userId}")
    ActivityRegistrationPO selectByActivityAndUser(@Param("activityId") Long activityId,
                                                     @Param("userId") Long userId);

    @Select("SELECT * FROM t_activity_registration WHERE activity_id = #{activityId} ORDER BY register_time ASC LIMIT #{offset}, #{size}")
    List<ActivityRegistrationPO> selectByActivityId(@Param("activityId") Long activityId,
                                                     @Param("offset") int offset,
                                                     @Param("size") int size);

    @Select("SELECT COUNT(*) FROM t_activity_registration WHERE activity_id = #{activityId} AND status = 'REGISTERED'")
    long countByActivityId(@Param("activityId") Long activityId);

    @Select("SELECT activity_id FROM t_activity_registration WHERE user_id = #{userId} AND status = 'REGISTERED'")
    List<Long> selectRegisteredActivityIds(@Param("userId") Long userId);

    @Select("SELECT * FROM t_activity_registration WHERE user_id = #{userId} ORDER BY register_time DESC")
    List<ActivityRegistrationPO> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM t_activity_registration WHERE activity_id = #{activityId} AND user_id = #{userId} AND status = 'REGISTERED'")
    int countByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
