package com.campus.infrastructure.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.infrastructure.config.WechatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 微信 API 客户端 — 生产实现
 * <p>
 * 仅在非 mock-wechat profile 时激活，真实调用微信服务端接口
 */
@Slf4j
@Component
@Profile("!mock-wechat")
@RequiredArgsConstructor
public class WechatApiClientImpl implements WechatApiClient {

    private final WechatProperties wechatProperties;

    /** 小程序：用 code 换取 openId + session_key */
    private static final String MINIPROGRAM_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code";

    /** 公众号 H5：用 code 换取 openId + access_token */
    private static final String OFFICIAL_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code";

    @Override
    public WechatSession code2Session(String code) {
        String url = buildUrl(code);

        log.info("请求微信 API: type={}, code={}", wechatProperties.getType(),
                code.substring(0, Math.min(10, code.length())) + "...");

        String responseBody;
        try {
            responseBody = HttpUtil.get(url);
        } catch (Exception e) {
            log.error("请求微信 API 网络异常", e);
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED.getCode(),
                    "微信服务连接失败，请稍后重试");
        }

        log.debug("微信 API 响应: {}", responseBody);

        JSONObject json = JSONUtil.parseObj(responseBody);

        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "未知错误");
            log.error("微信 API 返回错误: errcode={}, errmsg={}", errcode, errmsg);
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED.getCode(),
                    "微信登录失败: " + errmsg);
        }

        String openId = json.getStr("openid");
        if (StrUtil.isBlank(openId)) {
            log.error("微信 API 未返回 openId: {}", responseBody);
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        WechatSession session = new WechatSession();
        session.setOpenId(openId);
        session.setUnionId(json.getStr("unionid"));
        session.setSessionKey(json.getStr("session_key"));
        session.setAccessToken(json.getStr("access_token"));

        log.info("微信 code2Session 成功: openId={}", openId.substring(0, Math.min(10, openId.length())) + "...");
        return session;
    }

    private String buildUrl(String code) {
        String template = "miniprogram".equals(wechatProperties.getType())
                ? MINIPROGRAM_URL : OFFICIAL_URL;
        return StrUtil.format(template, wechatProperties.getAppId(),
                wechatProperties.getAppSecret(), code);
    }
}
