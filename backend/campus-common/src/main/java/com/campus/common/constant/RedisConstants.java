package com.campus.common.constant;

/**
 * Redis Key 常量（Cache-Aside 模式统一管理）
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    // ========== Key 前缀 ==========

    /** 短信验证码前缀 */
    public static final String SMS_CODE_PREFIX = "sms:code:";

    /** 登录 Token 前缀（token → 用户信息） */
    public static final String TOKEN_PREFIX = "user:token:";

    /** 用户信息缓存前缀（userId → User JSON） */
    public static final String USER_INFO_PREFIX = "user:info:";

    /** 用户名 → userId 映射前缀 */
    public static final String USERNAME_ID_PREFIX = "user:id:username:";

    /** 手机号 → userId 映射前缀 */
    public static final String PHONE_ID_PREFIX = "user:id:phone:";

    /** 邮箱 → userId 映射前缀 */
    public static final String EMAIL_ID_PREFIX = "user:id:email:";

    /** 微信 openId → userId 映射前缀 */
    public static final String OPENID_ID_PREFIX = "user:id:openid:";

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

    // ========== 过期时间 ==========

    /** 短信验证码过期时间：5分钟 */
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;

    /** 登录 Token 过期时间：24小时 */
    public static final int TOKEN_EXPIRE_HOURS = 24;

    /** 用户信息缓存过期时间：30分钟 */
    public static final int USER_INFO_EXPIRE_SECONDS = 1800;

    /** 映射缓存过期时间（username/phone→userId）：1小时 */
    public static final int MAPPING_EXPIRE_SECONDS = 3600;

    /** 权限缓存过期时间：1小时 */
    public static final int PERMISSION_EXPIRE_SECONDS = 3600;

    /** 登录失败次数过期时间：30分钟 */
    public static final int LOGIN_FAIL_EXPIRE_SECONDS = 1800;

    /** 验证码发送频率限制过期时间：60秒 */
    public static final int SMS_RATE_LIMIT_EXPIRE_SECONDS = 60;

    /** 验证码每日发送次数过期时间：24小时 */
    public static final int SMS_DAILY_LIMIT_EXPIRE_SECONDS = 86400;

    // ========== 限流阈值 ==========

    /** 同一手机号每日最大发送次数 */
    public static final int SMS_DAILY_MAX_COUNT = 5;

    /** 同一手机号发送频率限制（秒） */
    public static final int SMS_RATE_LIMIT_SECONDS = 60;

    /** 登录失败最大次数（超过锁定） */
    public static final int LOGIN_FAIL_MAX_COUNT = 10;

    // ==================== 通用 Key 构建方法 ====================

    /** 短信验证码 Key */
    public static String getSmsCodeKey(String phone) {
        return SMS_CODE_PREFIX + phone;
    }

    /** Token Key（token → 用户信息） */
    public static String getTokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    /** 用户 Token Key（userId → token，反向映射） */
    public static String getUserTokenKey(Long userId) {
        return TOKEN_PREFIX + userId;
    }

    /** 用户信息缓存 Key */
    public static String getUserInfoKey(Long userId) {
        return USER_INFO_PREFIX + userId;
    }

    /** 用户名 → userId 映射 Key */
    public static String getUsernameIdKey(String username) {
        return USERNAME_ID_PREFIX + username;
    }

    /** 手机号 → userId 映射 Key */
    public static String getPhoneIdKey(String phone) {
        return PHONE_ID_PREFIX + phone;
    }

    /** 邮箱 → userId 映射 Key */
    public static String getEmailIdKey(String email) {
        return EMAIL_ID_PREFIX + email;
    }

    /** 微信 openId → userId 映射 Key */
    public static String getOpenIdIdKey(String openId) {
        return OPENID_ID_PREFIX + openId;
    }

    /** 登录失败次数 Key */
    public static String getLoginFailKey(String username) {
        return LOGIN_FAIL_PREFIX + username;
    }

    /** 验证码发送频率限制 Key */
    public static String getSmsRateLimitKey(String phone) {
        return SMS_RATE_LIMIT_PREFIX + phone;
    }

    /** 验证码每日发送次数 Key */
    public static String getSmsDailyLimitKey(String phone) {
        return SMS_DAILY_LIMIT_PREFIX + phone;
    }

    /** 接口限流 Key */
    public static String getRateLimitKey(String apiPath, String ip) {
        return RATE_LIMIT_PREFIX + apiPath + ":" + ip;
    }
}
