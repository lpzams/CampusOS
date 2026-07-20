package com.campus.infrastructure.persistence.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassroomMapper extends BaseMapper<ClassroomPO> {

    @Select("SELECT * FROM t_classroom WHERE id = #{id} AND deleted = 0")
    ClassroomPO selectById(@Param("id") Long id);

    @Select("SELECT * FROM t_classroom WHERE building = #{building} AND name = #{name} AND deleted = 0")
    ClassroomPO selectByBuildingAndName(@Param("building") String building, @Param("name") String name);

    @Select("<script>SELECT * FROM t_classroom WHERE deleted = 0" +
            "<if test='building != null and building != \"\"'> AND building = #{building}</if>" +
            " ORDER BY building, floor, name LIMIT #{offset}, #{size}</script>")
    List<ClassroomPO> selectPage(@Param("building") String building, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_classroom WHERE deleted = 0" +
            "<if test='building != null and building != \"\"'> AND building = #{building}</if></script>")
    long count(@Param("building") String building);

    @Select("<script>SELECT * FROM t_classroom WHERE deleted = 0" +
            "<if test='building != null and building != \"\"'> AND building = #{building}</if>" +
            " ORDER BY building, floor, name</script>")
    List<ClassroomPO> selectAll(@Param("building") String building);

    @Select("SELECT DISTINCT s.classroom_id FROM t_schedule s WHERE s.day_of_week = #{dow} AND s.time_slot = #{ts}")
    List<Long> selectOccupiedIds(@Param("dow") int dow, @Param("ts") String ts);
}
