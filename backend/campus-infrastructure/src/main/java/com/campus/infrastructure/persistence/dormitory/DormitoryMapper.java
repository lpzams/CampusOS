package com.campus.infrastructure.persistence.dormitory;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
interface DormitoryMapper extends BaseMapper<DormitoryPO> {
    @Select("SELECT * FROM t_dormitory WHERE dormitory_id = #{dormitoryId}")
    DormitoryPO selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
}

@Mapper
interface DormitoryMemberMapper extends BaseMapper<DormitoryMemberPO> {
    @Select("SELECT d.* FROM t_dormitory_member m INNER JOIN t_dormitory d ON m.dormitory_id = d.dormitory_id WHERE m.user_id = #{userId}")
    DormitoryPO selectDormitoryByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_dormitory_member WHERE dormitory_id = #{dormitoryId}")
    List<DormitoryMemberPO> selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
}

@Mapper
interface DormitoryNoticeMapper extends BaseMapper<DormitoryNoticePO> {
    @Select("SELECT * FROM t_dormitory_notice WHERE deleted = 0 ORDER BY create_time DESC")
    List<DormitoryNoticePO> selectAll();
}

@Mapper
interface DormitoryUtilityMapper extends BaseMapper<DormitoryUtilityPO> {
    @Select("SELECT * FROM t_dormitory_utility WHERE dormitory_id = #{dormitoryId}")
    DormitoryUtilityPO selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
}

@Mapper
interface DormitoryUtilityRecordMapper extends BaseMapper<DormitoryUtilityRecordPO> {
    @Select("SELECT * FROM t_dormitory_utility_record WHERE dormitory_id = #{dormitoryId} ORDER BY record_month DESC")
    List<DormitoryUtilityRecordPO> selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
}
