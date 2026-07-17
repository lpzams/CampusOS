package com.campus.application.user.service;

import com.campus.common.api.ResultCode;
import com.campus.common.constant.RedisConstants;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final StringRedisTemplate stringRedisTemplate;

    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    /**
     * 发送验证码
     */
    public void sendSmsCode(String phone) {

        // 1. 生成6位验证码
        String code = generateCode();

        // 2. 存入 Redis（5分钟过期）
        String key = RedisConstants.getSmsCodeKey(phone);
        stringRedisTemplate.opsForValue().set(key, code, RedisConstants.CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 3. 调用短信平台发送
        log.info("验证码发送成功: phone={}, code={}", phone, code);
    }

    /**
     * 校验验证码
     */
    public void validateSmsCode(String phone, String inputCode) {
        String key = RedisConstants.getSmsCodeKey(phone);
        String cachedCode = stringRedisTemplate.opsForValue().get(key);

        // 1. 验证码是否存在
        if (cachedCode == null) {
            throw new BusinessException(ResultCode.SMS_CODE_EXPIRED.getCode(), "验证码已过期，请重新获取");
        }

        // 2. 验证码是否正确
        if (!cachedCode.equals(inputCode)) {
            throw new BusinessException(ResultCode.SMS_CODE_ERROR.getCode(), "验证码错误");
        }

        // 3. 校验通过，删除 Redis 中的验证码（防止重复使用）
        stringRedisTemplate.delete(key);
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}