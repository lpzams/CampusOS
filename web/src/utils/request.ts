/**
 * axios 统一封装 —— 整个前端只创建这一个请求实例。
 *
 * 【它做了三件事】
 *  1. 统一 baseURL（读 .env 的 VITE_API_BASE_URL，开发环境是 /api，由 vite 代理转发到后端）；
 *  2. 请求拦截器：自动携带 token；
 *  3. 响应拦截器：统一"拆包"后端的 Result{code,msg,data}，
 *     并自动处理 token 过期（1.8 刷新Token）。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/api/types'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// ===== 请求拦截器：自动携带 token =====
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ============================================================
// ===== 1.8 核心：响应拦截器 + Token 自动刷新 =====
// ============================================================

// 是否正在刷新 token（防止并发请求多次刷新）
let isRefreshing = false
// 等待队列：刷新期间挂起的请求
let pendingRequests: Array<{
  resolve: (value: any) => void
  reject: (reason?: any) => void
  config: any
}> = []

instance.interceptors.response.use(
  (response) => {
    const result = response.data as Result
    if (result.code === 200) {
      return result.data as never
    }
    // ===== 401 特殊处理：token 过期 =====
    if (result.code === 401) {
      // 如果是刷新 token 接口本身返回 401，说明 refresh token 也过期了，直接登出
      const isRefreshRequest = response.config.url?.includes('/auth/refresh')
      if (isRefreshRequest) {
        // 清除登录态，跳转登录
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(new Error('登录已过期'))
      }

      // 否则尝试刷新 token
      return new Promise((resolve, reject) => {
        // 将当前请求加入等待队列
        pendingRequests.push({ resolve, reject, config: response.config })

        // 如果已经在刷新中，不重复触发
        if (isRefreshing) return

        isRefreshing = true

        // 使用 store 刷新 token
        import('@/stores/user').then(({ useUserStore }) => {
          const userStore = useUserStore()
          userStore.refreshToken().then((newToken: any) => {
            if (newToken) {
              // 刷新成功，执行所有等待的请求
              pendingRequests.forEach(({ resolve, config }) => {
                // 更新请求头中的 token
                config.headers.Authorization = `Bearer ${newToken}`
                // 重发请求
                resolve(instance(config))
              })
            } else {
              // 刷新失败，拒绝所有等待的请求
              pendingRequests.forEach(({ reject }) => {
                reject(new Error('Token刷新失败'))
              })
              // 跳转登录页
              ElMessage.error('登录已过期，请重新登录')
              window.location.href = '/login'
            }
            // 清空等待队列
            pendingRequests = []
            isRefreshing = false
          }).catch(() => {
            // 刷新失败
            pendingRequests.forEach(({ reject }) => {
              reject(new Error('Token刷新失败'))
            })
            pendingRequests = []
            isRefreshing = false
            ElMessage.error('登录已过期，请重新登录')
            window.location.href = '/login'
          })
        })
      })
    }

    // 其他业务错误
    ElMessage.error(result.msg || '请求失败')
    return Promise.reject(new Error(result.msg || '请求失败'))
  },
  (error) => {
    // HTTP 层面的失败：后端没启动、404、超时等
    const msg = error.code === 'ECONNABORTED'
      ? '请求超时，请稍后重试'
      : '网络异常，请检查后端服务是否已启动（默认 http://localhost:8080）'
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

// ------------------------------------------------------------------
// 四个泛型快捷方法
// ------------------------------------------------------------------

export function get<T>(url: string, params?: object): Promise<T> {
  return instance.get(url, { params }) as unknown as Promise<T>
}

export function post<T>(url: string, data?: object): Promise<T> {
  return instance.post(url, data) as unknown as Promise<T>
}

export function put<T>(url: string, data?: object): Promise<T> {
  return instance.put(url, data) as unknown as Promise<T>
}

export function del<T>(url: string): Promise<T> {
  return instance.delete(url) as unknown as Promise<T>
}

// ===== 文件上传（FormData）=====
export function upload<T>(url: string, data: FormData): Promise<T> {
  return instance.post(url, data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }) as unknown as Promise<T>
}

export default instance