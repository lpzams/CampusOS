package com.campus.infrastructure.cache;

import com.campus.common.constant.RedisConstants;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户缓存服务 —— Cache-Aside 模式核心
 * <p>
 * 读：先查缓存 → 命中返回 / 未命中返回 null（调用方查 DB 后回填）
 * 写：DB 更新后删除对应缓存 Key，下次读取时自动从 DB 回填最新数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== Cache-Aside 读 ====================

    /**
     * 从缓存获取用户信息（一级缓存：user:info:{userId}）
     *
     * @return User 或 null（缓存未命中）
     */
    public User getUser(Long userId) {
        if (userId == null) return null;
        String key = RedisConstants.getUserInfoKey(userId);
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            log.debug("缓存命中: {}", key);
            return objectMapper.readValue(json, User.class);
        } catch (Exception e) {
            log.warn("反序列化用户缓存失败: key={}", key);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 通过用户名查 userId（二级映射缓存）
     */
    public Long getUserIdByUsername(String username) {
        if (username == null) return null;
        return getMappingValue(RedisConstants.getUsernameIdKey(username));
    }

    /**
     * 通过手机号查 userId（二级映射缓存）
     */
    public Long getUserIdByPhone(String phone) {
        if (phone == null) return null;
        return getMappingValue(RedisConstants.getPhoneIdKey(phone));
    }

    /**
     * 通过邮箱查 userId（二级映射缓存）
     */
    public Long getUserIdByEmail(String email) {
        if (email == null) return null;
        return getMappingValue(RedisConstants.getEmailIdKey(email));
    }

    /**
     * 通过微信 openId 查 userId（二级映射缓存）
     */
    public Long getUserIdByOpenId(String openId) {
        if (openId == null) return null;
        return getMappingValue(RedisConstants.getOpenIdIdKey(openId));
    }

    // ==================== Cache-Aside 回填 ====================

    /**
     * 将用户信息回填到缓存（DB 查询后调用）
     * <p>
     * 回填内容：
     * <ul>
     *   <li>一级缓存：user:info:{userId} → User JSON</li>
     *   <li>二级映射：username/phone/email/openId → userId</li>
     * </ul>
     */
    public void putUser(User user) {
        if (user == null || user.getId() == null) return;

        // 1. 一级缓存：用户完整信息
        try {
            String json = objectMapper.writeValueAsString(user);
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.getUserInfoKey(user.getId()),
                    json,
                    RedisConstants.USER_INFO_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );
            log.debug("回填用户缓存: userId={}", user.getId());
        } catch (JsonProcessingException e) {
            log.error("序列化 User 失败: userId={}", user.getId(), e);
            return;
        }

        // 2. 二级映射缓存：各唯一字段 → userId
        putMapping(RedisConstants.getUsernameIdKey(user.getUsername()), user.getId());
        putMapping(RedisConstants.getPhoneIdKey(user.getPhone()), user.getId());
        putMapping(RedisConstants.getEmailIdKey(user.getEmail()), user.getId());
        putMapping(RedisConstants.getOpenIdIdKey(user.getOpenId()), user.getId());
    }

    // ==================== Cache-Aside 删除（写操作后调用） ====================

    /**
     * DB 写操作后删除所有相关缓存 Key
     * <p>
     * 原则：先更新数据库 → 再删缓存。下次读操作自动从 DB 加载最新数据回填缓存。
     */
    public void evictUser(User user) {
        if (user == null || user.getId() == null) return;

        // 1. 删除一级缓存
        stringRedisTemplate.delete(RedisConstants.getUserInfoKey(user.getId()));

        // 2. 删除二级映射缓存（基于当前字段值）
        if (user.getUsername() != null) {
            stringRedisTemplate.delete(RedisConstants.getUsernameIdKey(user.getUsername()));
        }
        if (user.getPhone() != null) {
            stringRedisTemplate.delete(RedisConstants.getPhoneIdKey(user.getPhone()));
        }
        if (user.getEmail() != null) {
            stringRedisTemplate.delete(RedisConstants.getEmailIdKey(user.getEmail()));
        }
        if (user.getOpenId() != null) {
            stringRedisTemplate.delete(RedisConstants.getOpenIdIdKey(user.getOpenId()));
        }

        log.debug("删除用户缓存: userId={}", user.getId());
    }

    // ==================== Profile 缓存 ====================

    /**
     * 从缓存获取用户详情
     */
    public UserProfile getProfile(Long userId) {
        if (userId == null) return null;
        String key = RedisConstants.getUserProfileKey(userId);
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserProfile.class);
        } catch (Exception e) {
            log.warn("反序列化 Profile 缓存失败: key={}", key);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 回填用户详情缓存
     */
    public void putProfile(UserProfile profile) {
        if (profile == null || profile.getUserId() == null) return;
        try {
            String json = objectMapper.writeValueAsString(profile);
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.getUserProfileKey(profile.getUserId()),
                    json,
                    RedisConstants.USER_INFO_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("序列化 Profile 失败: userId={}", profile.getUserId(), e);
        }
    }

    /**
     * 删除用户详情缓存（写操作后调用）
     */
    public void evictProfile(Long userId) {
        if (userId == null) return;
        stringRedisTemplate.delete(RedisConstants.getUserProfileKey(userId));
        log.debug("删除 Profile 缓存: userId={}", userId);
    }

    // ==================== 私有工具方法 ====================

    private Long getMappingValue(String key) {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    private void putMapping(String key, Long userId) {
        if (key == null || userId == null) return;
        stringRedisTemplate.opsForValue().set(
                key,
                userId.toString(),
                RedisConstants.MAPPING_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );
    }
}
