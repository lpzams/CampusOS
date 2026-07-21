package com.campus.infrastructure.persistence.notice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoticeMapper extends BaseMapper<NoticePO> {

    @Select("<script>" +
            "SELECT * FROM t_notice WHERE deleted = 0 " +
            "<if test='type != null and type != \"\"'> AND type = #{type} </if>" +
            "<if test='department != null and department != \"\"'> AND department LIKE CONCAT('%',#{department},'%') </if>" +
            "ORDER BY is_top DESC, create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<NoticePO> selectPage(@Param("type") String type,
                               @Param("department") String department,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_notice WHERE deleted = 0 " +
            "<if test='type != null and type != \"\"'> AND type = #{type} </if>" +
            "<if test='department != null and department != \"\"'> AND department LIKE CONCAT('%',#{department},'%') </if>" +
            "</script>")
    long count(@Param("type") String type, @Param("department") String department);

    @Select("SELECT COUNT(*) FROM t_notice WHERE deleted = 0 AND type = #{type}")
    long countByType(@Param("type") String type);

    @Update("UPDATE t_notice SET read_count = read_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementReadCount(@Param("id") Long id);
}
