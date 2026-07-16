import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 应用入口：装配 Vue + Pinia(状态) + Router(路由) + Element Plus(UI)
// 说明：Element Plus 组件本身用 vite 的按需自动导入(见 vite.config.ts)，
// 这里 app.use(ElementPlus) 主要是为了 ElMessage / ElMessageBox 这类挂在全局的 API 可用。
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
