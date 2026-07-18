package com.campus.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 微信 API 客户端 — Mock 实现（开发/测试用）
 * <p>
 * 激活方式：spring.profiles.active: mock-wechat
 * <p>
 * Mock 行为：直接将请求中的 code 当作 openId 返回，方便前后端联调测试。
 * <p>
 * 测试请求示例：
 * <pre>
 * POST /api/user/login/wechat
 * {
 *   "code": "test_openid_001",
 *   "userInfo": {
 *     "nickName": "测试用户",
 *     "avatarUrl": "https://example.com/avatar.png"
 *   }
 * }
 * </pre>
 * 首次请求会自动注册用户，再次请求同一 openId 会直接登录。
 */
@Slf4j
@Component
@Profile("mock-wechat")
public class MockWechatApiClient implements WechatApiClient {

    @Override
    public WechatSession code2Session(String code) {
        log.info("[Mock] 微信 code2Session: code 直接作为 openId={}", code);

        WechatSession session = new WechatSession();
        session.setOpenId(code);
        session.setUnionId("mock_union_" + code);
        session.setSessionKey("mock_session_key");
        session.setAccessToken("mock_access_token");

        return session;
    }
}
