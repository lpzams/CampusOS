package com.campus.application.user.service;

import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===== 状态码映射（API ↔ 实体） =====
    // 实体: 0=正常 1=冻结 2=待审核 3=注销
    // API:  0=待审核 1=已启用 2=已禁用

    private static Integer apiStatusToEntity(Integer apiStatus) {
        if (apiStatus == null) return null;
        switch (apiStatus) {
            case 0: return 2;  // 待审核
            case 1: return 0;  // 已启用
            case 2: return 1;  // 已禁用
            default: return apiStatus;
        }
    }

    private static int entityStatusToApi(int entityStatus) {
        switch (entityStatus) {
            case 2: return 0;  // 待审核
            case 0: return 1;  // 已启用
            case 1: return 2;  // 已禁用
            default: return entityStatus;
        }
    }

    private static String getStatusName(int entityStatus) {
        switch (entityStatus) {
            case 0: return "已启用";
            case 1: return "已禁用";
            case 2: return "待审核";
            case 3: return "已注销";
            default: return String.valueOf(entityStatus);
        }
    }

    private static String getUserTypeName(int userType) {
        switch (userType) {
            case 1: return "学生";
            case 2: return "教师";
            case 3: return "管理员";
            default: return String.valueOf(userType);
        }
    }

    // ==================== 1.8 获取用户列表（管理员） ====================

    public PageResult<Map<String, Object>> getUserList(String keyword, Integer apiStatus,
                                                        Integer userType, int page, int size) {
        Integer entityStatus = apiStatusToEntity(apiStatus);
        int offset = (page - 1) * size;

        List<User> users = userRepository.findUsers(keyword, entityStatus, userType, offset, size);
        long total = userRepository.countUsers(keyword, entityStatus, userType);

        // 批量查询 profile 获取 studentId
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, UserProfile> profileMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<UserProfile> profiles = userProfileRepository.findByUserIds(userIds);
            profileMap = profiles.stream()
                    .collect(Collectors.toMap(UserProfile::getUserId, p -> p, (a, b) -> a));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users) {
            UserProfile profile = profileMap.get(u.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", u.getId());
            item.put("username", u.getUsername());
            item.put("realName", u.getRealName());
            item.put("userType", u.getUserType());
            item.put("userTypeName", getUserTypeName(u.getUserType()));
            item.put("phone", u.getPhone());
            item.put("email", u.getEmail());
            item.put("department", u.getDepartment());
            item.put("status", entityStatusToApi(u.getStatus() != null ? u.getStatus() : 0));
            item.put("statusName", getStatusName(u.getStatus() != null ? u.getStatus() : 0));
            item.put("createdTime", u.getCreatedTime() != null ? u.getCreatedTime().format(FORMATTER) : null);
            item.put("studentId", profile != null ? profile.getStudentId() : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 1.9 审核用户（管理员） ====================

    @Transactional
    public Map<String, Object> auditUser(Long userId, Map<String, Object> command) {
        User user = userRepository.findById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);

        int apiStatus = ((Number) command.get("status")).intValue();
        String remark = (String) command.get("remark");

        // API status: 1=审核通过 2=审核拒绝
        int newEntityStatus;
        if (apiStatus == 1) {
            newEntityStatus = 0; // 审核通过 → 正常
        } else if (apiStatus == 2) {
            newEntityStatus = 1; // 审核拒绝 → 禁用
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "审核状态无效");
        }

        user.setStatus(newEntityStatus);
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.update(user);

        log.info("审核用户: userId={}, status={}, remark={}", userId, apiStatus, remark);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("status", entityStatusToApi(newEntityStatus));
        result.put("statusName", getStatusName(newEntityStatus));
        result.put("auditTime", LocalDateTime.now().format(FORMATTER));
        result.put("remark", remark);
        return result;
    }

    // ==================== 1.10 启用/禁用用户（管理员） ====================

    @Transactional
    public Map<String, Object> updateUserStatus(Long userId, Map<String, Object> command) {
        User user = userRepository.findById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);

        int apiStatus = ((Number) command.get("status")).intValue();
        String remark = (String) command.get("remark");

        // API status: 1=启用 2=禁用
        int newEntityStatus;
        if (apiStatus == 1) {
            newEntityStatus = 0; // 启用 → 正常
        } else if (apiStatus == 2) {
            newEntityStatus = 1; // 禁用 → 冻结
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "状态值无效");
        }

        user.setStatus(newEntityStatus);
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.update(user);

        log.info("启用/禁用用户: userId={}, status={}, remark={}", userId, apiStatus, remark);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("status", entityStatusToApi(newEntityStatus));
        result.put("statusName", getStatusName(newEntityStatus));
        result.put("updateTime", LocalDateTime.now().format(FORMATTER));
        return result;
    }

    // ==================== 1.11 删除用户（管理员） ====================

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);

        // 逻辑删除：设置 status = 3（已注销）
        user.setStatus(3);
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.update(user);

        log.info("管理员删除用户: userId={}, username={}", userId, user.getUsername());
    }
}
