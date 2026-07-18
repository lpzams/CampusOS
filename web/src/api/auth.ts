/**
 * 登录认证模块 API —— 一个功能一个文件，集中放该功能的接口函数和类型。
 *
 * 每个函数对应后端 AuthController 的一个端点，类型对应后端的 DTO / VO。
 *
 * 【新增功能时】照抄本文件：建 api/你的功能.ts，
 * 把 URL 前缀换成 /你的功能，类型对着你后端的 DTO 写一遍。
 */
import { get, post, put, upload } from '../utils/request'
import type {
  Result,
  LoginParams,
  LoginResponse,
  SmsLoginParams,
  SendSmsParams,
  WechatLoginParams,
  WechatLoginResponse,
  RegisterParams,
  RegisterResponse,
  ForgotPasswordParams,
  RefreshTokenResponse,
  UserProfile,
  UpdateProfileParams,
  UpdateProfileResponse,
  AvatarUploadResponse,
  StudentProfile,
  TeacherProfile,
  VerifyParams,
  VerifyResponse,
  ChangePasswordParams,
} from './types'

// ============================================================
// ===== 1.1 账号密码登录 =====
// ============================================================

export function loginApi(data: LoginParams) {
  return post<Result<LoginResponse>>('/auth/login', data)
}

// ============================================================
// ===== 1.2 手机验证码登录 =====
// ============================================================

export function loginBySmsApi(data: SmsLoginParams) {
  return post<Result<LoginResponse>>('/auth/login/sms', data)
}

// ============================================================
// ===== 1.3 发送验证码 =====
// ============================================================

export function sendSmsCodeApi(data: SendSmsParams) {
  return post<Result<null>>('/auth/sms/send', data)
}

// ============================================================
// ===== 1.4 微信授权登录（小程序） =====
// ============================================================

export function wechatLoginApi(data: WechatLoginParams) {
  return post<Result<WechatLoginResponse>>('/auth/login/wechat', data)
}

// ============================================================
// ===== 1.5 用户注册 =====
// ============================================================

export function registerApi(data: RegisterParams) {
  return post<Result<RegisterResponse>>('/auth/register', data)
}

// ============================================================
// ===== 1.6 忘记密码 =====
// ============================================================

export function forgotPasswordApi(data: ForgotPasswordParams) {
  return post<Result<null>>('/auth/forgot-password', data)
}

// ============================================================
// ===== 1.7 退出登录 =====
// ============================================================

export function logoutApi() {
  return post<Result<null>>('/auth/logout')
}

// ============================================================
// ===== 1.8 刷新 Token =====
// ============================================================

export function refreshTokenApi() {
  return post<Result<RefreshTokenResponse>>('/auth/refresh')
}

// ============================================================
// ===== 2.1 获取个人信息 =====
// ============================================================

export function getUserProfileApi() {
  return get<Result<UserProfile>>('/user/profile')
}

// ============================================================
// ===== 2.2 修改个人信息 =====
// ============================================================

export function updateProfileApi(data: UpdateProfileParams) {
  return put<Result<UpdateProfileResponse>>('/user/profile', data)
}

// ============================================================
// ===== 2.3 修改头像 =====
// ============================================================

export function uploadAvatarApi(data: FormData) {
  return upload<Result<AvatarUploadResponse>>('/user/avatar', data)
}

// ============================================================
// ===== 2.4 获取学生详细信息 =====
// ============================================================

export function getStudentProfileApi() {
  return get<Result<StudentProfile>>('/user/profile/student')
}

// ============================================================
// ===== 2.5 获取教师详细信息 =====
// ============================================================

export function getTeacherProfileApi() {
  return get<Result<TeacherProfile>>('/user/profile/teacher')
}

// ============================================================
// ===== 2.6 实名认证 =====
// ============================================================

export function submitVerifyApi(data: VerifyParams) {
  return post<Result<VerifyResponse>>('/user/verify', data)
}

// ============================================================
// ===== 2.7 修改密码 =====
// ============================================================

export function changePasswordApi(data: ChangePasswordParams) {
  return put<Result<null>>('/user/password', data)
}