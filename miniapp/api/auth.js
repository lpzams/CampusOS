/**
 * 登录认证模块 API（小程序端）
 *
 * 对应 Web 端的 web/src/api/auth.ts，调同一批后端接口。
 *
 * 【新增功能时】照抄本文件：建 api/你的功能.js，URL 前缀换成 /你的功能。
 */
import { post } from '@/utils/request'

// ===== 1.1 账号密码登录（备用） =====
export function loginApi(data) {
  return post('/auth/login', data)
}

// ===== 1.2 短信验证码登录（备用） =====
export function loginBySmsApi(data) {
  return post('/auth/login/sms', data)
}

// ===== 1.3 发送验证码（备用） =====
export function sendSmsCodeApi(data) {
  return post('/auth/sms/send', data)
}

// ===== 1.4 微信授权登录（小程序主要用这个） =====

/**
 * 微信授权登录
 *
 * 对应后端接口：POST /auth/login/wechat
 *
 * 调用流程：
 * 1. 小程序调用 wx.login() 获取 code
 * 2. 调用本接口，将 code 传给后端
 * 3. 后端用 code 换取微信 openid 并完成登录
 *
 * @param {Object} data - { code, userInfo? }
 * @returns {Promise<{ token, userId, username, realName, userType, avatar, expiresIn }>}
 */
export function wechatLoginApi(data) {
  return post('/auth/login/wechat', data)
}