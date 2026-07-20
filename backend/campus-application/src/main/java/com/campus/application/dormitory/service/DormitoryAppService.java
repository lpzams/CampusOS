package com.campus.application.dormitory.service;

import com.campus.application.user.service.UserAppService;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.dormitory.entity.*;
import com.campus.domain.dormitory.repository.DormitoryRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormitoryAppService {

    private final DormitoryRepository dormitoryRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 10.1 获取宿舍信息 ====================

    public Map<String, Object> getDormitoryInfo(Long userId) {
        Dormitory dormitory = dormitoryRepository.findByUserId(userId);
        if (dormitory == null) {
            throw new BusinessException(ResultCode.DORMITORY_INFO_NOT_FOUND);
        }

        List<DormitoryMember> members = dormitoryRepository.findMembers(dormitory.getDormitoryId());
        DormitoryUtility utility = dormitoryRepository.findUtility(dormitory.getDormitoryId());

        // 解析 facilities JSON
        List<String> facilityList = new ArrayList<>();
        if (dormitory.getFacilities() != null && !dormitory.getFacilities().isEmpty()) {
            // 简单 JSON 数组解析（假设格式为 ["空调","热水器"]）
            String raw = dormitory.getFacilities().replaceAll("[\\[\\]\"]", "");
            for (String f : raw.split(",")) {
                String trimmed = f.trim();
                if (!trimmed.isEmpty()) facilityList.add(trimmed);
            }
        }

        // 成员信息
        List<Map<String, Object>> memberList = new ArrayList<>();
        for (DormitoryMember member : members) {
            User user = userRepository.findById(member.getUserId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", member.getUserId());
            m.put("realName", user != null ? user.getRealName() : "未知");
            m.put("phone", user != null ? user.getPhone() : "");
            m.put("isRoomLeader", member.getIsRoomLeader() != null && member.getIsRoomLeader() == 1);
            m.put("avatar", user != null ? user.getAvatar() : "");
            memberList.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", dormitory.getId());
        result.put("building", dormitory.getBuilding());
        result.put("room", dormitory.getRoom());
        result.put("type", dormitory.getTypeName());
        result.put("typeCode", dormitory.getType());
        result.put("members", memberList);
        result.put("facilities", facilityList);
        result.put("electricityBalance", utility != null ? utility.getElectricityBalance() : BigDecimal.ZERO);
        result.put("waterBalance", utility != null ? utility.getWaterBalance() : BigDecimal.ZERO);
        return result;
    }

    // ==================== 10.2 获取宿舍公告 ====================

    public List<Map<String, Object>> getDormitoryNotices() {
        List<DormitoryNotice> notices = dormitoryRepository.findAllNotices();
        List<Map<String, Object>> list = new ArrayList<>();
        for (DormitoryNotice notice : notices) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", notice.getId());
            item.put("title", notice.getTitle());
            item.put("content", notice.getContent());
            item.put("type", notice.getType());
            item.put("createTime", notice.getCreateTime() != null ? notice.getCreateTime().format(FORMATTER) : null);
            list.add(item);
        }
        return list;
    }

    // ==================== 10.3 查询水电余额 ====================

    public Map<String, Object> getDormitoryUtility(Long userId) {
        Dormitory dormitory = dormitoryRepository.findByUserId(userId);
        if (dormitory == null) {
            throw new BusinessException(ResultCode.DORMITORY_INFO_NOT_FOUND);
        }

        DormitoryUtility utility = dormitoryRepository.findUtility(dormitory.getDormitoryId());
        List<DormitoryUtilityRecord> records = dormitoryRepository.findUtilityRecords(dormitory.getDormitoryId());

        // 历史记录
        List<Map<String, Object>> history = new ArrayList<>();
        for (DormitoryUtilityRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", record.getRecordMonth());
            item.put("consumption", record.getElectricityConsumption());
            item.put("cost", record.getElectricityCost());
            history.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dormitoryId", dormitory.getDormitoryId());
        result.put("electricityBalance", utility != null ? utility.getElectricityBalance() : BigDecimal.ZERO);
        result.put("waterBalance", utility != null ? utility.getWaterBalance() : BigDecimal.ZERO);
        result.put("lastUpdateTime", utility != null && utility.getUpdateTime() != null ?
                utility.getUpdateTime().format(FORMATTER) : null);
        result.put("electricityHistory", history);
        return result;
    }
}
