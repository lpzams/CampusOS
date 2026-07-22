/**
 * 认证模块 API（功能 1：用户登录与统一认证）。
 *
 * 对应后端 AuthController（/api/auth/**）。登录/注册成功后返回 LoginResult，
 * 页面拿到后调用 stores/user.ts 的 login() 保存登录态，
 * 之后 request.ts 会自动在每个请求头带上 Authorization: Bearer {token}。
 */
import { post } from '@/utils/request'

/** 登录/注册成功的返回（后端 AuthAppService.loginResult：用户资料 + token） */
export interface LoginResult {
  token: string
  userId: number
  username: string
  realName?: string
  /** 1-学生 2-教师 3-管理员 */
  userType: number
  avatar?: string
  phone?: string
  email?: string
  department?: string
  /** token 有效期（毫秒） */
  expiresIn?: number
  [key: string]: unknown
}

/** 注册表单（对应后端 /auth/register 的请求体） */
export interface RegisterForm {
  username: string
  password: string
  realName: string
  phone: string
  email?: string
  /** 1-学生 2-教师（管理员不开放注册） */
  userType: number
  department?: string
  /** 学生填学号、教师填工号 */
  studentId?: string
}

/** 账号密码登录：POST /api/auth/login（种子账号 admin / 123456） */
export function login(data: { username: string; password: string }) {
  return post<LoginResult>('/auth/login', data)
}

/** 手机验证码登录：POST /api/auth/login/sms */
export function loginBySms(data: { phone: string; code: string }) {
  return post<LoginResult>('/auth/login/sms', data)
}

/** 发送验证码：POST /api/auth/sms/send（本地演示模式后端会把验证码直接返回，方便联调） */
export function sendSmsCode(phone: string) {
  return post<{ sent: boolean; code?: string }>('/auth/sms/send', { phone, type: 'LOGIN' })
}

/** 注册（成功即自动登录，返回结构同 login）：POST /api/auth/register */
export function register(data: RegisterForm) {
  return post<LoginResult>('/auth/register', data)
}

/** 忘记密码（凭手机验证码重置）：POST /api/auth/forgot-password */
export function forgotPassword(data: { username: string; phone: string; code: string; newPassword: string }) {
  return post<void>('/auth/forgot-password', data)
}

/** 退出登录：POST /api/auth/logout（后端无状态，前端同时要清本地登录态） */
export function logout() {
  return post<void>('/auth/logout')
}
