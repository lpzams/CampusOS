package com.campus.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置属性
 * <p>
 * 在 application.yml 中配置：
 * <pre>
 * wechat:
 *   app-id: wx1234567890
 *   app-secret: abc123def456
 *   type: miniprogram
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    /** 微信 AppID */
    private String appId;

    /** 微信 AppSecret */
    private String appSecret;

    /** 类型：miniprogram-小程序 / official-公众号H5 */
    private String type = "miniprogram";
}
