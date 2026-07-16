/// <reference types="vite/client" />

// 给 .env 文件里 VITE_ 开头的变量补充类型，
// 这样代码里写 import.meta.env.VITE_API_BASE_URL 才有类型提示。
interface ImportMetaEnv {
  /** API 基础路径，见 .env.development / .env.production */
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
