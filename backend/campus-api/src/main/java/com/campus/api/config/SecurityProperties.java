package com.campus.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置属性（白名单路径统一管理）
 * <p>
 * 在 application.yml 中配置：
 * <pre>
 * security:
 *   whitelist:
 *     - /api/user/login
 *     - /api/user/login/sms
 *     - ...
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 白名单路径列表（不需要登录即可访问）
     */
    private List<String> whitelist = new ArrayList<>();
}
