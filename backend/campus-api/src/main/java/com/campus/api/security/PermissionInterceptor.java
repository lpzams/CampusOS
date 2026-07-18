package com.campus.api.security;

import com.campus.common.api.ResultCode;
import com.campus.common.context.LoginUserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器
 * <p>
 * 仅拦截需要登录的接口（如 /api/user/**，排除白名单路径）：
 * <ul>
 *   <li>ThreadLocal 中存在用户信息 → 放行</li>
 *   <li>ThreadLocal 中不存在 → 返回 401（未授权）</li>
 * </ul>
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (LoginUserHolder.get() != null) {
            return true;
        }

        log.debug("未授权访问: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}",
                        ResultCode.UNAUTHORIZED.getCode(), "未授权，请先登录")
        );
        return false;
    }
}
