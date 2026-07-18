/**
 * 用户状态（Pinia store）。
 *
 * 【Pinia 是什么】跨页面共享的响应式状态。谁 import 谁就能读写，
 * 比如顶栏要显示用户名，登录页要写入用户名，二者通过这个 store 共享。
 *
 * 【现状】1.1 ~ 1.8 登录认证模块已完成，2.1 ~ 2.7 用户模块已完成。
 * 登录成功后自动保存 token 和用户信息到 localStorage，刷新页面自动恢复登录态。
 * 登录成功后自动获取个人信息（2.1），并根据用户类型获取学生/教师详细信息（2.4/2.5）。
 *
 * 【新增功能时】需要跨页面共享状态才建 store（stores/你的功能.ts）；
 * 只在单个页面内用的数据，直接写在组件里即可，不要什么都塞进 store。
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type {
  LoginResponse,
  UserProfile,
  UpdateProfileParams,
  StudentProfile,
  TeacherProfile,
  ChangePasswordParams,
} from '@/api/types'
import {
  loginApi,
  loginBySmsApi,
  logoutApi,
  refreshTokenApi,
  getUserProfileApi,
  updateProfileApi,
  uploadAvatarApi,
  getStudentProfileApi,
  getTeacherProfileApi,
  changePasswordApi,
} from '@/api/auth'

export type UserRole = 'student' | 'teacher' | 'admin' | 'guest'

export const useUserStore = defineStore('user', () => {
  // ============================================================
  // ========== State ==========
  // ============================================================

  /** 登录 token（从 localStorage 初始化，刷新不丢） */
  const token = ref<string>(localStorage.getItem('token') || '')

  /** 用户登录信息（从 localStorage 恢复） */
  const userInfo = ref<LoginResponse | null>(
    localStorage.getItem('userInfo')
      ? JSON.parse(localStorage.getItem('userInfo')!)
      : null
  )

  /** 用户完整个人信息（2.1 获取） */
  const profile = ref<UserProfile | null>(null)

  /** 学生详细信息（2.4 获取，仅学生用户有值） */
  const studentProfile = ref<StudentProfile | null>(null)

  /** 教师详细信息（2.5 获取，仅教师用户有值） */
  const teacherProfile = ref<TeacherProfile | null>(null)

  // ============================================================
  // ========== Getter（计算属性） ==========
  // ============================================================

  /** 是否已登录：有 token 且有用户信息即视为已登录 */
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  /** 显示用的名字（优先 profile，其次 userInfo，最后显示"访客"） */
  const name = computed(() => profile.value?.realName || userInfo.value?.realName || '访客')

  /** 用户头像 URL */
  const avatar = computed(() => profile.value?.avatar || userInfo.value?.avatar || '')

  /** 用户角色（将后端 userType 数字映射为字符串） */
  const role = computed<UserRole>(() => {
    const type = profile.value?.userType || userInfo.value?.userType
    if (type === 1) return 'student'
    if (type === 2) return 'teacher'
    if (type === 3) return 'admin'
    return 'guest'
  })

  /** 是否为管理员 */
  const isAdmin = computed(() => role.value === 'admin')

  /** 是否为学生 */
  const isStudent = computed(() => role.value === 'student')

  /** 是否为教师 */
  const isTeacher = computed(() => role.value === 'teacher')

  // ============================================================
  // ========== 通用工具方法 ==========
  // ============================================================

  /** 保存登录态到 localStorage */
  function persistLogin(data: LoginResponse) {
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))
  }

  /** 清除所有登录态 */
  function clearLoginState() {
    token.value = ''
    userInfo.value = null
    profile.value = null
    studentProfile.value = null
    teacherProfile.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // ============================================================
  // ========== 1.1 账号密码登录 ==========
  // ============================================================

  async function login(username: string, password: string) {
    try {
      const res = await loginApi({ username, password })
      if (res.code === 200 && res.data) {
        token.value = res.data.token
        userInfo.value = res.data
        persistLogin(res.data)
        // 登录成功后获取个人信息
        await fetchProfile()
        // 根据用户类型获取详细信息
        if (isStudent.value) {
          await fetchStudentProfile()
        } else if (isTeacher.value) {
          await fetchTeacherProfile()
        }
        return true
      }
      return false
    } catch {
      return false
    }
  }

  // ============================================================
  // ========== 1.2 手机验证码登录 ==========
  // ============================================================

  async function loginBySms(phone: string, code: string) {
    try {
      const res = await loginBySmsApi({ phone, code })
      if (res.code === 200 && res.data) {
        token.value = res.data.token
        userInfo.value = res.data
        persistLogin(res.data)
        await fetchProfile()
        if (isStudent.value) {
          await fetchStudentProfile()
        } else if (isTeacher.value) {
          await fetchTeacherProfile()
        }
        return true
      }
      return false
    } catch {
      return false
    }
  }

  // ============================================================
  // ========== 1.7 退出登录 ==========
  // ============================================================

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 即使接口失败，前端也要清理
    } finally {
      clearLoginState()
    }
  }

  // ============================================================
  // ========== 1.8 刷新 Token ==========
  // ============================================================

  async function refreshToken(): Promise<string | null> {
    try {
      const res = await refreshTokenApi()
      if (res.code === 200 && res.data?.token) {
        const newToken = res.data.token
        token.value = newToken
        localStorage.setItem('token', newToken)
        return newToken
      }
      return null
    } catch {
      await logout()
      return null
    }
  }

  // ============================================================
  // ========== 2.1 获取个人信息 ==========
  // ============================================================

  async function fetchProfile() {
    try {
      const res = await getUserProfileApi()
      if (res.code === 200 && res.data) {
        profile.value = res.data
        // 同步更新 userInfo 中的姓名和头像
        if (userInfo.value) {
          userInfo.value.realName = res.data.realName
          userInfo.value.avatar = res.data.avatar
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
        return res.data
      }
      return null
    } catch {
      return null
    }
  }

  // ============================================================
  // ========== 2.2 修改个人信息 ==========
  // ============================================================

  async function updateProfile(data: UpdateProfileParams) {
    try {
      const res = await updateProfileApi(data)
      if (res.code === 200 && res.data) {
        profile.value = res.data
        if (userInfo.value) {
          userInfo.value.realName = res.data.realName
          userInfo.value.avatar = res.data.avatar
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
        return res.data
      }
      return null
    } catch {
      return null
    }
  }

  // ============================================================
  // ========== 2.3 修改头像 ==========
  // ============================================================

  async function updateAvatar(file: File) {
    try {
      const formData = new FormData()
      formData.append('file', file)

      const res = await uploadAvatarApi(formData)
      if (res.code === 200 && res.data?.avatar) {
        const newAvatar = res.data.avatar
        if (profile.value) {
          profile.value.avatar = newAvatar
        }
        if (userInfo.value) {
          userInfo.value.avatar = newAvatar
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
        return newAvatar
      }
      return null
    } catch {
      return null
    }
  }

  // ============================================================
  // ========== 2.4 获取学生详细信息 ==========
  // ============================================================

  async function fetchStudentProfile() {
    try {
      const res = await getStudentProfileApi()
      if (res.code === 200 && res.data) {
        studentProfile.value = res.data
        return res.data
      }
      return null
    } catch {
      return null
    }
  }

  // ============================================================
  // ========== 2.5 获取教师详细信息 ==========
  // ============================================================

  async function fetchTeacherProfile() {
    try {
      const res = await getTeacherProfileApi()
      if (res.code === 200 && res.data) {
        teacherProfile.value = res.data
        return res.data
      }
      return null
    } catch {
      return null
    }
  }

  // ============================================================
  // ========== 2.7 修改密码 ==========
  // ============================================================

  async function changePassword(data: ChangePasswordParams) {
    try {
      const res = await changePasswordApi(data)
      if (res.code === 200) {
        // 修改成功，清除登录态，让用户重新登录
        await logout()
        return true
      }
      return false
    } catch {
      return false
    }
  }

  // ============================================================
  // ========== 导出 ==========
  // ============================================================

  return {
    // State
    token,
    userInfo,
    profile,
    studentProfile,
    teacherProfile,
    // Getters
    isLoggedIn,
    name,
    avatar,
    role,
    isAdmin,
    isStudent,
    isTeacher,
    // Actions
    login,
    loginBySms,
    logout,
    refreshToken,
    fetchProfile,
    updateProfile,
    updateAvatar,
    fetchStudentProfile,
    fetchTeacherProfile,
    changePassword,
  }
})