package com.campus.application.auth;

import com.campus.common.exception.BusinessException;
import com.campus.domain.auth.CredentialService;
import com.campus.domain.shared.CampusRecord;
import com.campus.domain.shared.CampusRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthAppService {
    private final CampusRecordRepository users;
    private final CredentialService credentials;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, SmsCode> smsCodes = new ConcurrentHashMap<>();

    public AuthAppService(CampusRecordRepository users, CredentialService credentials) {
        this.users = users;
        this.credentials = credentials;
    }

    public Map<String, Object> login(String username, String password) {
        ensureDemoUsers();
        CampusRecord user = find("username", username);
        if (!credentials.matches(password, String.valueOf(user.getData().get("passwordHash"))))
            throw new BusinessException(401, "用户名或密码错误");
        if (Boolean.TRUE.equals(user.getData().get("mustResetPassword")))
            throw new BusinessException(403, "该教师账户待身份认领并设置独立密码");
        return loginResult(user);
    }

    public Map<String, Object> loginSms(String phone, String code) {
        verifySms(phone, code);
        return loginResult(find("phone", phone));
    }

    public Map<String, Object> loginWechat(String code, Map<String, Object> userInfo) {
        if (code == null || code.isBlank()) throw new BusinessException(400, "微信 code 不能为空");
        String username = "wx_" + Integer.toUnsignedString(code.hashCode());
        try { return loginResult(find("username", username)); }
        catch (BusinessException ignored) {
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("username", username);
            user.put("realName", userInfo == null ? "微信用户" : userInfo.getOrDefault("nickName", "微信用户"));
            user.put("avatar", userInfo == null ? "" : userInfo.getOrDefault("avatarUrl", ""));
            user.put("userType", 1);
            user.put("roleCode", "ROLE_STUDENT");
            user.put("passwordHash", credentials.hash(code));
            return loginResult(users.save(new CampusRecord(null, "user", user)));
        }
    }

    @Transactional
    public Map<String, Object> register(Map<String, Object> body) {
        String username = required(body, "username");
        String password = required(body, "password");
        int userType;
        try { userType = Integer.parseInt(required(body, "userType")); }
        catch (NumberFormatException e) { throw new BusinessException(400, "userType 必须为 1 或 2"); }
        if (userType != 1 && userType != 2) throw new BusinessException(400, "userType 必须为 1 或 2");
        if (password.length() < 6) throw new BusinessException(400, "密码至少 6 位");
        if (users.findByType("user").stream().anyMatch(u -> username.equals(u.getData().get("username"))))
            throw new BusinessException(400, "用户名已存在");
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.remove("password");
        data.put("passwordHash", credentials.hash(password));
        data.put("roleCode", userType == 1 ? "ROLE_STUDENT" : "ROLE_TEACHER");
        data.putIfAbsent("status", 0);
        data.put("createdTime", LocalDateTime.now().toString());
        CampusRecord saved = users.save(new CampusRecord(null, "user", data));
        return loginResult(saved);
    }

    public String sendSms(String phone) {
        if (phone == null || !phone.matches("1\\d{10}")) throw new BusinessException(400, "手机号格式错误");
        String code = String.format("%06d", random.nextInt(1_000_000));
        smsCodes.put(phone, new SmsCode(code, Instant.now().plusSeconds(300)));
        return code;
    }

    @Transactional
    public void resetPassword(Map<String, Object> body) {
        CampusRecord user = find("username", required(body, "username"));
        String phone = required(body, "phone");
        if (!phone.equals(user.getData().get("phone"))) throw new BusinessException(400, "账号与手机号不匹配");
        verifySms(phone, required(body, "code"));
        user.getData().put("passwordHash", credentials.hash(required(body, "newPassword")));
        users.save(user);
    }

    public Map<String, Object> refresh(String token) {
        try {
            CredentialService.TokenClaims claims = credentials.parseToken(token);
            return Map.of("token", credentials.createToken(claims.userId(), claims.username(), claims.userType()));
        } catch (Exception e) { throw new BusinessException(401, "Token 无效或已过期"); }
    }

    public CredentialService.TokenClaims parse(String token) { return credentials.parseToken(token); }

    public Map<String, Object> profile(Long userId) { return sanitize(getUser(userId)); }

    public Map<String, Object> studentProfile(Long userId) {
        CampusRecord user = getUser(userId);
        if (!Integer.valueOf(1).equals(number(user.getData().get("userType"))))
            throw new BusinessException(403, "当前用户不是学生");
        return sanitize(user);
    }

    public Map<String, Object> teacherProfile(Long userId) {
        CampusRecord user = getUser(userId);
        if (!Integer.valueOf(2).equals(number(user.getData().get("userType"))))
            throw new BusinessException(403, "当前用户不是教师");
        return sanitize(user);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, Map<String, Object> changes) {
        CampusRecord user = getUser(userId);
        changes.keySet().removeIf(key -> !java.util.Set.of("realName", "phone", "email", "gender", "avatar").contains(key));
        user.getData().putAll(changes);
        users.save(user);
        return sanitize(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        CampusRecord user = getUser(userId);
        if (!credentials.matches(oldPassword, String.valueOf(user.getData().get("passwordHash"))))
            throw new BusinessException(400, "原密码错误");
        if (newPassword == null || newPassword.length() < 6) throw new BusinessException(400, "新密码至少 6 位");
        user.getData().put("passwordHash", credentials.hash(newPassword));
        users.save(user);
    }

    @Transactional
    public Map<String, Object> verifyIdentity(Long userId, Map<String, Object> body) {
        String realName = required(body, "realName");
        String idCard = required(body, "idCard");
        String studentId = required(body, "studentId");
        if (!idCard.matches("\\d{17}[0-9Xx]")) throw new BusinessException(400, "身份证号格式错误");
        CampusRecord user = getUser(userId);
        if (user.getData().get("studentId") != null && !studentId.equals(user.getData().get("studentId")))
            throw new BusinessException(400, "学号或工号与账号不匹配");
        user.getData().put("realName", realName);
        user.getData().put("identityVerified", false);
        user.getData().put("identityStatus", "审核中");
        users.save(user);
        return Map.of("verified", false, "status", "审核中");
    }

    private CampusRecord getUser(Long id) { return users.findById("user", id).orElseThrow(() -> new BusinessException(404, "用户不存在")); }

    private CampusRecord find(String key, String value) {
        return users.findByType("user").stream().filter(u -> value.equals(String.valueOf(u.getData().get(key))))
                .findFirst().orElseThrow(() -> new BusinessException(401, "账号不存在"));
    }

    /**
     * 兼容已存在但未跑过初始化脚本的数据库。只补缺失的演示账号，绝不覆盖现有用户资料或密码。
     */
    @Transactional
    void ensureDemoUsers() {
        seedDemoUser("admin", "管理员", "13800138000", 3, "ROLE_ADMIN", null);
        seedDemoUser("student", "演示学生", "13800138001", 1, "ROLE_STUDENT", "20260001");
        seedDemoUser("teacher", "演示教师", "13800138002", 2, "ROLE_TEACHER", "T20260001");
    }

    private void seedDemoUser(String username, String realName, String phone, int userType, String roleCode, String studentId) {
        if (users.findByType("user").stream().anyMatch(user -> username.equals(user.getData().get("username")))) return;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("passwordHash", credentials.hash("123456"));
        data.put("realName", realName);
        data.put("phone", phone);
        data.put("userType", userType);
        data.put("roleCode", roleCode);
        data.put("status", 0);
        if (studentId != null) data.put("studentId", studentId);
        users.save(new CampusRecord(null, "user", data));
    }

    private Map<String, Object> loginResult(CampusRecord user) {
        int status = number(user.getData().get("status")) == null ? 0 : number(user.getData().get("status"));
        if (status != 0) throw new BusinessException(403, status == 2 ? "账号正在审核中" : "账号已被禁用或注销");
        Map<String, Object> data = sanitize(user);
        data.put("token", credentials.createToken(user.getId(), String.valueOf(data.get("username")), ((Number)data.get("userType")).intValue()));
        data.put("userId", user.getId());
        data.put("expiresIn", 7_200_000);
        return data;
    }

    private Map<String, Object> sanitize(CampusRecord user) {
        Map<String, Object> data = new LinkedHashMap<>(user.getData());
        data.remove("passwordHash");
        data.put("id", user.getId());
        return data;
    }

    private void verifySms(String phone, String code) {
        SmsCode saved = smsCodes.remove(phone);
        if (saved == null || saved.expiresAt().isBefore(Instant.now()) || !saved.code().equals(code))
            throw new BusinessException(400, "验证码错误或已过期");
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) throw new BusinessException(400, key + " 不能为空");
        return value.toString();
    }

    private Integer number(Object value) { return value instanceof Number number ? number.intValue() : null; }

    private record SmsCode(String code, Instant expiresAt) {}
}
