package com.campus.api.security;

import com.campus.common.api.ResultCode;
import com.campus.common.constant.RedisConstants;
import com.campus.common.context.LoginUser;
import com.campus.common.context.LoginUserHolder;
import com.campus.infrastructure.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 认证拦截器
 * <p>
 * 拦截所有 /api/** 请求：
 * <ul>
 *   <li>无 Token → 直接放行</li>
 *   <li>有 Token → 校验 JWT 签名 + Redis 中存在性</li>
 *   <li>校验通过 → 用户信息存入 ThreadLocal，刷新 Redis 过期时间（续期 2h）</li>
 *   <li>校验失败 → 返回 401</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = extractToken(request);

        // 无 Token → 放行
        if (token == null || token.isEmpty()) {
            return true;
        }

        // ===== 有 Token → 校验 =====

        // 1. 校验 JWT 签名
        if (!jwtUtil.validateToken(token)) {
            log.debug("JWT 签名校验失败: token={}", maskToken(token));
            write401(response, ResultCode.TOKEN_INVALID);
            return false;
        }

        // 2. 校验 Redis 中是否存在（是否被登出）
        String tokenKey = RedisConstants.getTokenKey(token);
        String userJson = stringRedisTemplate.opsForValue().get(tokenKey);
        if (userJson == null || userJson.isEmpty()) {
            log.debug("Redis 中不存在 Token: {}", maskToken(token));
            write401(response, ResultCode.TOKEN_EXPIRED);
            return false;
        }

        // 3. 反序列化用户信息 → 存入 ThreadLocal
        try {
            LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);
            LoginUserHolder.set(loginUser);
            log.debug("认证成功: userId={}, username={}", loginUser.getUserId(), loginUser.getUsername());
        } catch (Exception e) {
            log.error("反序列化 LoginUser 失败", e);
            write401(response, ResultCode.TOKEN_INVALID);
            return false;
        }

        // 4. 刷新 Redis 过期时间（令牌自动续期 2h）
        stringRedisTemplate.expire(tokenKey, RedisConstants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束后清除 ThreadLocal，防止内存泄漏
        LoginUserHolder.remove();
    }

    /**
     * 从请求头中提取 Token（Bearer xxx）
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Token 脱敏：首20位...末10位，兼顾调试与安全
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 34) {
            return token;  // 短 token 直接输出（异常情况）
        }
        return token.substring(0, 20) + "..." + token.substring(token.length() - 10);
    }

    /**
     * 返回 401 响应
     */
    private void write401(HttpServletResponse response, ResultCode resultCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}",
                        resultCode.getCode(), resultCode.getMessage())
        );
    }
}
