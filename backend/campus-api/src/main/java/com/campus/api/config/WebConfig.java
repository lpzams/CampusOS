package com.campus.api.config;

import com.campus.api.security.AuthInterceptor;
import com.campus.api.security.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置 —— 注册双拦截器
 * <p>
 * 拦截器执行顺序：
 * <ol>
 *   <li>AuthInterceptor（认证拦截器）— 拦截所有 /api/**，解析 Token → ThreadLocal</li>
 *   <li>PermissionInterceptor（权限拦截器）— 拦截需登录的路径，检查 ThreadLocal</li>
 * </ol>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;
    private final SecurityProperties securityProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ===== 1. 认证拦截器：拦截所有 /api/** =====
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        // ===== 2. 权限拦截器：仅拦截需要登录的接口 =====
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(securityProperties.getWhitelist())
                .order(2);
    }
}
