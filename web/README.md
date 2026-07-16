# campus-web —— 网站前端（Vue3 + TypeScript + Element Plus）

高校智慧校园门户系统的网站端。当前以 **新闻模块** 为全链路示例，
其余 14 个功能模块照着它的写法扩展即可（见 `docs/新增功能指南.md`）。

## 技术栈

| 依赖 | 用途 |
| --- | --- |
| Vue 3 + `<script setup>` + TypeScript | 页面框架 |
| Vite | 开发服务器 / 打包 |
| Vue Router | 路由（`src/router/index.ts`） |
| Pinia | 跨页面状态（`src/stores/`） |
| Element Plus | UI 组件库（已配按需自动导入） |
| Axios | HTTP 请求（统一封装在 `src/utils/request.ts`） |

## 本地运行

```bash
# 0. 前置：后端已启动（默认 http://localhost:8080），数据库已执行 docs/sql/init.sql
# 1. 安装依赖（Node 18+）
npm install
# 2. 启动开发服务器，浏览器打开 http://localhost:5173
npm run dev
```

开发环境的 `/api` 请求由 Vite 代理转发到后端（见 `vite.config.ts` 的 `server.proxy`），
所以不存在跨域问题；生产环境的后端地址在 `.env.production` 里配置。

## 目录结构（src/）

```
src/
├── main.ts               # 应用入口：装配 Pinia / Router / Element Plus
├── App.vue               # 全站布局：顶栏导航 + 内容区 + 页脚
├── router/index.ts       # 路由表（加页面 = 加一条路由）
├── api/
│   ├── types.ts          # Result / PageResult 等与后端约定的通用类型
│   └── news.ts           # 新闻模块接口（一个功能一个文件）★模板
├── utils/
│   ├── request.ts        # axios 封装：统一 baseURL、拆包 Result、错误提示
│   └── format.ts         # 时间格式化等展示工具
├── stores/
│   └── user.ts           # Pinia 示例：用户状态（登录模块完成后接入真实数据）
└── views/
    ├── news/
    │   ├── NewsListView.vue    # 列表页（搜索 + 分页）★读页面模板
    │   └── NewsDetailView.vue  # 详情页（路由参数）
    └── admin/
        └── NewsManageView.vue  # 管理页（表格 + 弹窗表单）★写页面模板
```

## 怎么加一个新页面（30 秒版）

1. `src/api/你的功能.ts`：照抄 `api/news.ts`，写接口函数和类型；
2. `src/views/你的功能/Xxx.vue`：照抄 `views/news/` 下最像的页面；
3. `src/router/index.ts`：加一条路由；
4. 需要进顶部导航时，在 `App.vue` 的 `el-menu` 里加一个 `el-menu-item`。

完整说明（含后端、小程序端步骤）见仓库根目录 `docs/新增功能指南.md`。
