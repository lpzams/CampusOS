// 小程序入口 —— uni-app Vue3 的固定写法（createSSRApp 是官方要求，别改成 createApp）。
// 【新增功能时】不用改这里。要装全局插件（如 Pinia）时才在 createApp 里 app.use(...)。
import App from './App'
import { createSSRApp } from 'vue'

export function createApp() {
  const app = createSSRApp(App)
  return { app }
}
