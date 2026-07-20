package com.campus.infrastructure.persistence.location;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

// ===== POs =====

@Data @TableName("t_location")
class LocationPO {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String category;
    private Double longitude;
    private Double latitude;
    private String address;
    private String building;
    private String image;
    private String description;
    private String facilities;
    @TableField("open_time") private String openTime;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}

@Data @TableName("t_location_floor")
class LocationFloorPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("location_id") private Long locationId;
    private Integer floor;
    private String rooms;
}

// ===== Mappers =====

@Mapper
interface LocationMapper extends BaseMapper<LocationPO> {

    @Select("<script>SELECT * FROM t_location WHERE deleted = 0 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "ORDER BY id ASC</script>")
    List<LocationPO> selectAll(@Param("category") String category);

    @Select("SELECT * FROM t_location WHERE deleted = 0 AND " +
            "(name LIKE CONCAT('%', #{keyword}, '%') OR address LIKE CONCAT('%', #{keyword}, '%') OR building LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY id ASC")
    List<LocationPO> searchByKeyword(@Param("keyword") String keyword);
}

@Mapper
interface LocationFloorMapper extends BaseMapper<LocationFloorPO> {
    @Select("SELECT * FROM t_location_floor WHERE location_id = #{locationId} ORDER BY floor ASC")
    List<LocationFloorPO> selectByLocationId(@Param("locationId") Long locationId);
}
