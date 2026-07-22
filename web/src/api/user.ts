/**
 * 个人信息中心 API（功能 2）。
 *
 * 对应后端 UserController（/api/user/**，全部需要登录）。
 * 后端用户数据是动态 Map（CampusRecord），字段随注册信息而定，
 * 所以 UserProfile 里大部分字段是可选的，页面展示时注意判空。
 */
import { get, post, put, upload } from '@/utils/request'

/** 用户资料（对应后端 AuthAppService.sanitize 的返回：用户全部字段去掉密码哈希） */
export interface UserProfile {
  id: number
  username: string
  realName?: string
  gender?: string
  phone?: string
  email?: string
  avatar?: string
  /** 1-学生 2-教师 3-管理员 */
  userType: number
  department?: string
  major?: string
  className?: string
  /** 学生学号 / 教师工号 */
  studentId?: string
  enrollmentYear?: string
  /** 教师：职称 / 研究方向 */
  title?: string
  researchField?: string
  /** 实名认证状态（提交过认证才有值，如"审核中"） */
  identityStatus?: string
  status?: number
  createdTime?: string
  [key: string]: unknown
}

/** 修改资料表单（后端只接受这四个字段，其余会被忽略） */
export interface UpdateProfileForm {
  realName?: string
  phone?: string
  email?: string
  gender?: string
}

/** 获取个人信息：GET /api/user/profile */
export function getProfile() {
  return get<UserProfile>('/user/profile')
}

/** 修改个人信息（返回更新后的完整资料）：PUT /api/user/profile */
export function updateProfile(data: UpdateProfileForm) {
  return put<UserProfile>('/user/profile', data)
}

/** 上传头像（multipart，字段名 file；返回更新后的完整资料）：POST /api/user/avatar */
export function uploadAvatar(file: File) {
  return upload<UserProfile>('/user/avatar', file)
}

/** 学生详细信息（非学生账号会报 403）：GET /api/user/profile/student */
export function getStudentProfile() {
  return get<UserProfile>('/user/profile/student')
}

/** 教师详细信息（非教师账号会报 403）：GET /api/user/profile/teacher */
export function getTeacherProfile() {
  return get<UserProfile>('/user/profile/teacher')
}

/** 实名认证（演示流程：提交后进入"审核中"）：POST /api/user/verify */
export function verifyIdentity(data: { realName: string; idCard: string; studentId: string }) {
  return post<{ verified: boolean; status: string }>('/user/verify', data)
}

/** 修改密码：PUT /api/user/password */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return put<void>('/user/password', data)
}
