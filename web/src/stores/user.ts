/**
 * 用户状态（Pinia store）—— 登录态的唯一权威来源。
 *
 * 【Pinia 是什么】跨页面共享的响应式状态。谁 import 谁就能读写，
 * 比如顶栏要显示用户名，登录页要写入用户名，二者通过这个 store 共享。
 *
 * 【持久化】token 和用户信息存 localStorage，刷新页面不丢；
 * request.ts 的请求拦截器从这里拿 token 带给后端，
 * 收到 401 时会调用 logout() 清空登录态并跳登录页。
 *
 * 【新增功能时】需要跨页面共享状态才建 store（stores/你的功能.ts）；
 * 只在单个页面内用的数据，直接写在组件里即可，不要什么都塞进 store。
 */
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginResult } from '@/api/auth'

export type UserRole = 'student' | 'teacher' | 'admin' | 'guest'

/** 教师不具备学生个人事务和学生消费类功能。管理员拥有全部功能。 */
const TEACHER_RESTRICTED_PATHS = [
  '/score', '/score-statistics', '/exam', '/exam-calendar', '/payment',
  '/electricity-recharge', '/card', '/dormitory', '/utility', '/repair',
  '/market', '/my-activities',
]

export function canRoleAccess(role: UserRole, path: string): boolean {
  if (path.startsWith('/admin/')) return role === 'admin'
  if (path.startsWith('/teaching')) return role === 'teacher' || role === 'admin'
  if (path.startsWith('/course-selection') || path.startsWith('/course-reviews')) return role !== 'teacher' && role !== 'guest'
  if (role === 'admin' || role === 'student') return true
  if (role !== 'teacher') return false
  return !TEACHER_RESTRICTED_PATHS.some(restricted => path === restricted || path.startsWith(`${restricted}/`))
}

const TOKEN_KEY = 'campus_token'
const USER_KEY = 'campus_user'

/** userType(1/2/3) -> 角色名，与后端约定：1-学生 2-教师 3-管理员 */
function toRole(userType?: number): UserRole {
  if (userType === 1) return 'student'
  if (userType === 2) return 'teacher'
  if (userType === 3) return 'admin'
  return 'guest'
}

/** 从 localStorage 恢复上次登录的用户信息（token 失效时后端会报 401 再清理） */
function restoreUser(): Partial<LoginResult> {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) ?? '{}')
  } catch {
    return {}
  }
}

export const useUserStore = defineStore('user', () => {
  const saved = restoreUser()

  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  /** 显示用的名字 */
  const name = ref(String(saved.realName || saved.username || '访客'))
  /** 角色：控制"资讯管理"这类入口只对 admin 可见 */
  const role = ref<UserRole>(token.value ? toRole(Number(saved.userType)) : 'guest')
  const userId = ref<number | null>(saved.userId != null ? Number(saved.userId) : null)
  const avatar = ref(String(saved.avatar ?? ''))

  const isLoggedIn = computed(() => token.value !== '')
  const isAdmin = computed(() => role.value === 'admin')
  const isStudent = computed(() => role.value === 'student')
  const isTeacher = computed(() => role.value === 'teacher')
  const canAccess = (path: string) => canRoleAccess(role.value, path)

  /** 登录/注册成功后调用：写入内存 + localStorage（data 是后端 /auth/login 的返回值） */
  function login(data: LoginResult) {
    token.value = data.token
    name.value = String(data.realName || data.username || '用户')
    role.value = toRole(Number(data.userType))
    userId.value = data.userId != null ? Number(data.userId) : null
    avatar.value = String(data.avatar ?? '')
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data))
  }

  /** 个人中心改了资料后同步显示（只更新展示字段，token 不动） */
  function updateDisplay(patch: { realName?: string; avatar?: string }) {
    if (patch.realName) name.value = patch.realName
    if (patch.avatar !== undefined) avatar.value = patch.avatar
    const merged = { ...restoreUser(), ...patch }
    localStorage.setItem(USER_KEY, JSON.stringify(merged))
  }

  /** 退出登录 / token 失效时调用 */
  function logout() {
    token.value = ''
    name.value = '访客'
    role.value = 'guest'
    userId.value = null
    avatar.value = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, name, role, userId, avatar, isLoggedIn, isAdmin, isStudent, isTeacher, canAccess, login, updateDisplay, logout }
})
