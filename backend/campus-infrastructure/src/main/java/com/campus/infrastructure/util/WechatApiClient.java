package com.campus.infrastructure.util;

import lombok.Data;

/**
 * 微信 API 客户端接口
 * <p>
 * 生产环境：调用微信服务端真实接口<br>
 * 测试环境：Mock 实现，直接将 code 作为 openId 返回
 */
public interface WechatApiClient {

    /**
     * 用微信授权 code 换取 openId
     *
     * @param code 微信授权临时 code（Mock 模式下直接作为 openId）
     * @return WechatSession
     */
    WechatSession code2Session(String code);

    // ==================== 内部类 ====================

    @Data
    class WechatSession {
        private String openId;
        private String unionId;
        private String sessionKey;
        private String accessToken;
    }
}
