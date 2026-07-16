/**
 * 用户状态（Pinia store 示例）。
 *
 * 【Pinia 是什么】跨页面共享的响应式状态。谁 import 谁就能读写，
 * 比如顶栏要显示用户名，登录页要写入用户名，二者通过这个 store 共享。
 *
 * 【现状】登录模块（功能 1）还没做，这里先放一个"访客"占位；
 * 做登录时：在登录成功回调里调用 login()，并把 token 存起来
 * （token 的使用位置见 utils/request.ts 的请求拦截器）。
 *
 * 【新增功能时】需要跨页面共享状态才建 store（stores/你的功能.ts）；
 * 只在单个页面内用的数据，直接写在组件里即可，不要什么都塞进 store。
 */
import { ref } from 'vue'
import { defineStore } from 'pinia'

export type UserRole = 'student' | 'teacher' | 'admin' | 'guest'

export const useUserStore = defineStore('user', () => {
  /** 显示用的名字 */
  const name = ref('访客')
  /** 角色：以后用来控制"新闻管理"这类入口只对 admin 可见 */
  const role = ref<UserRole>('guest')

  /** 登录成功后调用（由未来的登录页触发） */
  function login(userName: string, userRole: UserRole) {
    name.value = userName
    role.value = userRole
  }

  /** 退出登录 */
  function logout() {
    name.value = '访客'
    role.value = 'guest'
  }

  return { name, role, login, logout }
})
