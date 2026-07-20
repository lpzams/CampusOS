package com.campus.infrastructure.persistence.dormitory;

import com.campus.domain.dormitory.entity.*;
import com.campus.domain.dormitory.repository.DormitoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DormitoryRepositoryImpl implements DormitoryRepository {
    private final DormitoryMapper dormitoryMapper;
    private final DormitoryMemberMapper memberMapper;
    private final DormitoryNoticeMapper noticeMapper;
    private final DormitoryUtilityMapper utilityMapper;
    private final DormitoryUtilityRecordMapper utilityRecordMapper;

    @Override
    public Dormitory findByDormitoryId(String dormitoryId) {
        DormitoryPO po = dormitoryMapper.selectByDormitoryId(dormitoryId);
        if (po == null) return null;
        Dormitory d = new Dormitory();
        BeanUtils.copyProperties(po, d);
        return d;
    }

    @Override
    public Dormitory findByUserId(Long userId) {
        DormitoryPO po = memberMapper.selectDormitoryByUserId(userId);
        if (po == null) return null;
        Dormitory d = new Dormitory();
        BeanUtils.copyProperties(po, d);
        return d;
    }

    @Override
    public List<DormitoryMember> findMembers(String dormitoryId) {
        List<DormitoryMemberPO> poList = memberMapper.selectByDormitoryId(dormitoryId);
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(po -> {
            DormitoryMember m = new DormitoryMember();
            BeanUtils.copyProperties(po, m);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public DormitoryUtility findUtility(String dormitoryId) {
        DormitoryUtilityPO po = utilityMapper.selectByDormitoryId(dormitoryId);
        if (po == null) return null;
        DormitoryUtility u = new DormitoryUtility();
        BeanUtils.copyProperties(po, u);
        return u;
    }

    @Override
    public List<DormitoryUtilityRecord> findUtilityRecords(String dormitoryId) {
        List<DormitoryUtilityRecordPO> poList = utilityRecordMapper.selectByDormitoryId(dormitoryId);
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(po -> {
            DormitoryUtilityRecord r = new DormitoryUtilityRecord();
            BeanUtils.copyProperties(po, r);
            return r;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DormitoryNotice> findAllNotices() {
        List<DormitoryNoticePO> poList = noticeMapper.selectAll();
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(po -> {
            DormitoryNotice n = new DormitoryNotice();
            BeanUtils.copyProperties(po, n);
            return n;
        }).collect(Collectors.toList());
    }
}
