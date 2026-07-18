package com.campus.infrastructure.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;  // ← 改这里

    /**
     * 保存验证码
     */
    public void saveSmsCode(String phone, String code, int expireSeconds) {
        String key = "sms:code:" + phone;
        stringRedisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取验证码
     */
    public String getSmsCode(String phone) {
        String key = "sms:code:" + phone;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除验证码
     */
    public void deleteSmsCode(String phone) {
        String key = "sms:code:" + phone;
        stringRedisTemplate.delete(key);
    }

    // ========== 通用方法 ==========

    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}