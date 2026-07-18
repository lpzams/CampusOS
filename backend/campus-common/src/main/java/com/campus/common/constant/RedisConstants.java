package com.campus.common.constant;

import java.util.concurrent.TimeUnit;

/**
 * Redis 常量
 */
public final class RedisConstants {

    private RedisConstants() {
        // 私有构造器，防止实例化
    }

    // ========== Key 前缀 ==========

    /** 短信验证码前缀 */
    public static final String SMS_CODE_PREFIX = "sms:code:";
    /** 短信验证码过期时间 */
    public static final int CODE_EXPIRE_SECONDS = 300;

    /** 登录 Token 前缀 */
    public static final String TOKEN_PREFIX = "user:token:";

    /** 用户信息缓存前缀 */
    public static final String USER_INFO_PREFIX = "user:info:";

    /** 权限缓存前缀 */
    public static final String PERMISSION_PREFIX = "auth:permission:";

    /** 登录失败次数前缀 */
    public static final String LOGIN_FAIL_PREFIX = "user:fail:";

    /** 接口限流前缀 */
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    /** 验证码发送频率限制前缀 */
    public static final String SMS_RATE_LIMIT_PREFIX = "sms:rate:";

    /** 验证码每日发送次数前缀 */
    public static final String SMS_DAILY_LIMIT_PREFIX = "sms:daily:";

    // ========== 过期时间（秒） ==========

    /** 短信验证码过期时间：5分钟 */
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;

    /** 登录 Token 过期时间：2小时 */
    public static final int TOKEN_EXPIRE_HOURS = 24;

    /** 用户信息缓存过期时间：30分钟 */
    public static final int USER_INFO_EXPIRE_SECONDS = 1800;

    /** 权限缓存过期时间：1小时 */
    public static final int PERMISSION_EXPIRE_SECONDS = 3600;

    /** 登录失败次数过期时间：30分钟 */
    public static final int LOGIN_FAIL_EXPIRE_SECONDS = 1800;

    /** 验证码发送频率限制过期时间：60秒 */
    public static final int SMS_RATE_LIMIT_EXPIRE_SECONDS = 60;

    /** 验证码每日发送次数过期时间：24小时 */
    public static final int SMS_DAILY_LIMIT_EXPIRE_SECONDS = 86400;

    // ========== 限流 ==========

    /** 同一手机号每日最大发送次数 */
    public static final int SMS_DAILY_MAX_COUNT = 5;

    /** 同一手机号发送频率限制（秒） */
    public static final int SMS_RATE_LIMIT_SECONDS = 60;

    /** 登录失败最大次数（超过锁定） */
    public static final int LOGIN_FAIL_MAX_COUNT = 10;

    // ========== 通用方法 ==========

    /**
     * 获取验证码 Redis Key
     */
    public static String getSmsCodeKey(String phone) {
        return SMS_CODE_PREFIX + phone;
    }

    /**
     * 获取 Token Redis Key
     */
    public static String getTokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    /**
     * 获取用户信息缓存 Key
     */
    public static String getUserInfoKey(Long userId) {
        return USER_INFO_PREFIX + userId;
    }

    /**
     * 获取登录失败次数 Key
     */
    public static String getLoginFailKey(String username) {
        return LOGIN_FAIL_PREFIX + username;
    }

    /**
     * 获取验证码发送频率限制 Key
     */
    public static String getSmsRateLimitKey(String phone) {
        return SMS_RATE_LIMIT_PREFIX + phone;
    }

    /**
     * 获取验证码每日发送次数 Key
     */
    public static String getSmsDailyLimitKey(String phone) {
        return SMS_DAILY_LIMIT_PREFIX + phone;
    }

    /**
     * 获取接口限流 Key
     */
    public static String getRateLimitKey(String apiPath, String ip) {
        return RATE_LIMIT_PREFIX + apiPath + ":" + ip;
    }

    public static String getUserTokenKey(Long id) {
        return TOKEN_PREFIX + id;
    }
}