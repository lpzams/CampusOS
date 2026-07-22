package com.campus.application.auth;

import com.campus.common.api.PageResult;
import com.campus.common.exception.BusinessException;
import com.campus.domain.auth.CredentialService;
import com.campus.domain.course.teaching.CrawlerTeachingRepository;
import com.campus.domain.shared.CampusRecord;
import com.campus.domain.shared.CampusRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Admin user lifecycle and role management for the CampusRecord user store. */
@Service
public class AdminUserAppService {
    private static final Map<Integer, String> ROLE_CODES = Map.of(1, "ROLE_STUDENT", 2, "ROLE_TEACHER", 3, "ROLE_ADMIN");
    private final CampusRecordRepository users;
    private final CredentialService credentials;
    private final CrawlerTeachingRepository crawlerTeaching;

    public AdminUserAppService(CampusRecordRepository users, CredentialService credentials, CrawlerTeachingRepository crawlerTeaching) {
        this.users = users;
        this.credentials = credentials;
        this.crawlerTeaching = crawlerTeaching;
    }

    public PageResult<Map<String, Object>> page(String keyword, Integer status, Integer userType, int pageNum, int pageSize) {
        int safePage = Math.max(1, pageNum);
        int safeSize = Math.min(100, Math.max(1, pageSize));
        Stream<CampusRecord> stream = users.findByType("user").stream();
        if (keyword != null && !keyword.isBlank()) {
            String needle = keyword.trim().toLowerCase();
            stream = stream.filter(user -> contains(user, "username", needle) || contains(user, "realName", needle)
                    || contains(user, "phone", needle) || contains(user, "studentId", needle));
        }
        if (status != null) stream = stream.filter(user -> status.equals(number(user.getData().get("status"), 0)));
        if (userType != null) stream = stream.filter(user -> userType.equals(number(user.getData().get("userType"), 1)));
        List<Map<String, Object>> all = stream.sorted(Comparator.comparing(CampusRecord::getId).reversed()).map(this::summary).toList();
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(safePage, safeSize, all.size(), all.subList(from, to));
    }

    @Transactional
    public Map<String, Object> audit(Long userId, int status, String remark) {
        if (status != 0 && status != 1) throw new BusinessException(400, "审核状态只能为 0（通过）或 1（拒绝）");
        return updateStatus(userId, status, remark);
    }

    @Transactional
    public Map<String, Object> updateStatus(Long userId, int status, String remark) {
        if (status < 0 || status > 3) throw new BusinessException(400, "账号状态无效");
        CampusRecord user = getUser(userId);
        user.getData().put("status", status);
        user.getData().put("statusRemark", remark == null ? "" : remark);
        user.getData().put("updatedTime", LocalDateTime.now().toString());
        return summary(users.save(user));
    }

    /** Enables every non-cancelled account. Placeholder crawler teachers remain password-gated until claimed. */
    @Transactional
    public int enableAllActiveAccounts() {
        int enabled = 0;
        for (CampusRecord user : users.findByType("user")) {
            int status = number(user.getData().get("status"), 0);
            if (status == 0 || status == 3) continue;
            user.getData().put("status", 0);
            user.getData().put("statusRemark", "管理员批量启用");
            user.getData().put("updatedTime", LocalDateTime.now().toString());
            users.save(user);
            enabled++;
        }
        return enabled;
    }

    @Transactional
    public Map<String, Object> updateRole(Long userId, int userType) {
        String roleCode = ROLE_CODES.get(userType);
        if (roleCode == null) throw new BusinessException(400, "角色只能为学生、教师或管理员");
        CampusRecord user = getUser(userId);
        user.getData().put("userType", userType);
        user.getData().put("roleCode", roleCode);
        user.getData().put("updatedTime", LocalDateTime.now().toString());
        return summary(users.save(user));
    }

    @Transactional
    public void deactivate(Long userId) {
        CampusRecord user = getUser(userId);
        user.getData().put("status", 3);
        user.getData().put("updatedTime", LocalDateTime.now().toString());
        users.save(user);
    }

    @Transactional
    public Map<String, Object> claimCrawlerTeacher(Long userId, String password) {
        if (password == null || password.length() < 8) throw new BusinessException(400, "认领账户密码至少 8 位");
        CampusRecord user = getUser(userId);
        if (!"course_review_catalog".equals(user.getData().get("accountSource"))) {
            throw new BusinessException(400, "该账户不是待认领的课程评价教师账户");
        }
        if (!crawlerTeaching.markTeacherClaimed(userId)) throw new BusinessException(400, "教师身份目录不存在");
        user.getData().put("passwordHash", credentials.hash(password));
        user.getData().put("mustResetPassword", false);
        user.getData().put("identityStatus", "CLAIMED");
        user.getData().put("status", 0);
        user.getData().put("statusRemark", "教师身份已认领");
        user.getData().put("updatedTime", LocalDateTime.now().toString());
        return summary(users.save(user));
    }

    private CampusRecord getUser(Long id) {
        return users.findById("user", id).orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private Map<String, Object> summary(CampusRecord user) {
        Map<String, Object> data = user.getData();
        int userType = number(data.get("userType"), 1);
        int status = number(data.get("status"), 0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", data.get("username"));
        result.put("realName", data.get("realName"));
        result.put("phone", data.get("phone"));
        result.put("email", data.get("email"));
        result.put("department", data.get("department"));
        result.put("studentId", data.get("studentId"));
        result.put("userType", userType);
        result.put("roleCode", data.getOrDefault("roleCode", ROLE_CODES.get(userType)));
        result.put("status", status);
        result.put("statusRemark", data.getOrDefault("statusRemark", ""));
        result.put("createdTime", data.get("createdTime"));
        return result;
    }

    private boolean contains(CampusRecord user, String field, String needle) {
        Object value = user.getData().get(field);
        return value != null && value.toString().toLowerCase().contains(needle);
    }

    private int number(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }
}
