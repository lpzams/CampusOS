/**
 * axios 统一封装 —— 整个前端只创建这一个请求实例。
 *
 * 【它做了四件事】
 *  1. 统一 baseURL（读 .env 的 VITE_API_BASE_URL，开发环境是 /api，由 vite 代理转发到后端）；
 *  2. 请求拦截器：登录后自动带上 Authorization: Bearer {token}（token 存在 stores/user.ts）；
 *  3. 响应拦截器：统一"拆包"后端的 Result{code,msg,data} —— 业务代码拿到的直接就是 data，
 *     code !== 0 时统一弹错误提示并 reject，页面里不用每个接口都写 if (res.code === 0)；
 *  4. 401（未登录/token 过期）时清空登录态并跳转登录页，登录后回到原页面。
 *
 * 【新增功能时】不用改这里，直接在 api/你的功能.ts 里 import { get, post, put, del } 使用。
 */
import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/api/types'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// ===== 请求拦截器：发出去之前统一加工 =====
instance.interceptors.request.use((config) => {
  // 登录后把 token 带给后端（后端 AuthInterceptor 校验 @Authenticated 接口）
  const userStore = useUserStore()
  if (userStore.token) config.headers.Authorization = `Bearer ${userStore.token}`
  return config
})

/** 未登录/token 过期：清登录态，跳登录页并记住来路（防止重复弹窗跳转，加个标记） */
let redirectingToLogin = false
function handleUnauthorized(msg: string) {
  const userStore = useUserStore()
  userStore.logout()
  if (redirectingToLogin) return
  redirectingToLogin = true
  ElMessage.warning(msg || '请先登录')
  const current = router.currentRoute.value
  router
    .push({ path: '/login', query: current.path === '/login' ? {} : { redirect: current.fullPath } })
    .finally(() => { redirectingToLogin = false })
}

// ===== 响应拦截器：回来之后统一拆包 =====
instance.interceptors.response.use(
  (response) => {
    const result = response.data as Result
    // 后端约定：code === 0 才是成功（见 backend ResultCode.SUCCESS），非 0 都是失败
    if (result.code === 0) {
      // 只把业务数据往下传。注意：从这里开始，Promise 的值已经不是 AxiosResponse 了，
      // 所以下面的 get/post 封装里做了一次类型断言。
      return result.data as never
    }
    // 401/2001：后端 BusinessException(401) 与 ResultCode.UNAUTHORIZED 都表示登录态失效
    if (result.code === 401 || result.code === 2001) {
      handleUnauthorized(result.msg)
      return Promise.reject(new Error(result.msg || '请先登录'))
    }
    // 其它业务失败（后端 GlobalExceptionHandler 统一返回的 code/msg）
    ElMessage.error(result.msg || '请求失败')
    return Promise.reject(new Error(result.msg || '请求失败'))
  },
  (error) => {
    const msg = error.code === 'ECONNABORTED'
      ? '请求超时，请稍后重试'
      : error.response?.data?.msg
        || (error.response ? `请求失败（HTTP ${error.response.status}）` : '网络异常，请检查后端服务是否已启动（默认 http://localhost:8080）')
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

// ------------------------------------------------------------------
// 泛型快捷方法。T 填「后端 data 字段的类型」，例如：
//   get<PageResult<NewsItem>>('/news', { pageNum: 1 })
// ------------------------------------------------------------------

/** GET 请求，params 会拼成 query string（?a=1&b=2） */
export function get<T>(url: string, params?: object): Promise<T> {
  return instance.get(url, { params }) as unknown as Promise<T>
}

/** POST 请求，data 以 JSON body 发送 */
export function post<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
  return instance.post(url, data, config) as unknown as Promise<T>
}

/** PUT 请求 */
export function put<T>(url: string, data?: object): Promise<T> {
  return instance.put(url, data) as unknown as Promise<T>
}

/** DELETE 请求（delete 是 JS 关键字，故取名 del） */
export function del<T>(url: string, data?: object): Promise<T> {
  return instance.delete(url, data ? { data } : undefined) as unknown as Promise<T>
}

/** 文件上传（multipart/form-data），field 是后端 @RequestParam 的参数名 */
export function upload<T>(url: string, file: File, field = 'file'): Promise<T> {
  const form = new FormData()
  form.append(field, file)
  return instance.post(url, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }) as unknown as Promise<T>
}
