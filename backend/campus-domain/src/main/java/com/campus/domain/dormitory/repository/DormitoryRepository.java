package com.campus.domain.dormitory.repository;

import com.campus.domain.dormitory.entity.Dormitory;
import com.campus.domain.dormitory.entity.DormitoryMember;
import com.campus.domain.dormitory.entity.DormitoryNotice;
import com.campus.domain.dormitory.entity.DormitoryUtility;
import com.campus.domain.dormitory.entity.DormitoryUtilityRecord;
import java.util.List;

public interface DormitoryRepository {
    Dormitory findByDormitoryId(String dormitoryId);
    Dormitory findByUserId(Long userId);
    List<DormitoryMember> findMembers(String dormitoryId);
    DormitoryUtility findUtility(String dormitoryId);
    List<DormitoryUtilityRecord> findUtilityRecords(String dormitoryId);
    List<DormitoryNotice> findAllNotices();
}
