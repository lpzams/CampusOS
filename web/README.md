# campus-web —— 网站前端（Vue3 + TypeScript + Element Plus）

高校智慧校园门户系统的网站端。**15 个功能模块已全部实现**，
新闻模块是最早的全链路示例，其余模块都照着它的写法扩展（见 `docs/新增功能指南.md`）。

## 技术栈

| 依赖 | 用途 |
| --- | --- |
| Vue 3 + `<script setup>` + TypeScript | 页面框架 |
| Vite | 开发服务器 / 打包 |
| Vue Router | 路由（`src/router/index.ts`，含登录守卫） |
| Pinia | 跨页面状态（`src/stores/`，登录态持久化到 localStorage） |
| Element Plus | UI 组件库（已配按需自动导入） |
| Axios | HTTP 请求（统一封装在 `src/utils/request.ts`，自动带 token、401 跳登录） |

## 本地运行

```bash
# 0. 前置：后端已启动（默认 http://localhost:8080），数据库用项目根目录 docker compose up -d 一键启动
# 1. 安装依赖（Node 18+）
npm install
# 2. 启动开发服务器，浏览器打开 http://localhost:5173
npm run dev
```

开发环境的 `/api`、`/uploads` 请求由 Vite 代理转发到后端（见 `vite.config.ts` 的 `server.proxy`），
所以不存在跨域问题；生产环境由 nginx 做同样的转发（见 `docker/nginx.conf`）。

演示账号：`admin / 123456`（管理员，能看到「资讯管理」入口）；也可在注册页自建学生/教师账号。

## 功能模块 -> 页面对照

| # | 模块 | 路由 | 页面 |
| --- | --- | --- | --- |
| 1 | 登录与统一认证 | `/login` `/register` | `views/auth/`（账号/验证码登录、注册、忘记密码） |
| 2 | 个人信息中心 | `/profile` | `views/profile/`（资料/头像/学籍/实名认证/改密） |
| 3 | 校园新闻资讯 | `/news` `/admin/news` | `views/news/`、`views/admin/` |
| 4 | 公告通知 | `/notice` | `views/notice/`（列表/详情/已读/未读数） |
| 5 | 课程查询 | `/schedule` | `views/academic/ScheduleView.vue`（周课表/今日课程） |
| 6 | 成绩查询 | `/score` | `views/academic/ScoreView.vue`（GPA 统计 + 明细） |
| 7 | 考试安排 | `/exam` | `views/academic/ExamView.vue` |
| 8 | 校园缴费 | `/payment` | `views/life/PaymentView.vue`（待缴费/记录） |
| 9 | 校园卡 | `/card` | `views/life/CardView.vue`（余额/充值/挂失/流水） |
| 10 | 宿舍管理 | `/dormitory` | `views/life/DormitoryView.vue`（信息/成员/水电/公告） |
| 11 | 校园报修 | `/repair` | `views/life/RepairView.vue`（提交/进度/评价） |
| 12 | 二手交易 | `/market` | `views/market/`（列表/详情/发布/收藏/留言） |
| 13 | 校园活动 | `/activity` | `views/activity/`（列表/详情/报名/签到） |
| 14 | 地图导航 | `/map` | `views/map/MapView.vue`（地点/搜索/路径规划） |
| 15 | AI 助手 | `/assistant` | `views/assistant/AssistantView.vue`（问答/流程/推荐） |

需要登录的页面（教务、生活服务、个人中心等）在路由 `meta.requiresAuth` 标记，
未登录访问会跳到 `/login?redirect=原地址`，登录后自动跳回。

## 目录结构（src/）

```
src/
├── main.ts               # 应用入口：装配 Pinia / Router / Element Plus
├── App.vue               # 全站布局：顶栏导航（分组菜单 + 用户区）+ 内容区 + 页脚
├── router/index.ts       # 路由表 + 登录守卫（加页面 = 加一条路由）
├── api/                  # 一个功能一个文件：接口函数 + 类型（对着后端 Controller 写）
│   ├── types.ts          # Result / PageResult 等通用类型
│   ├── auth.ts user.ts notice.ts course.ts score.ts exam.ts
│   ├── payment.ts card.ts dormitory.ts repair.ts
│   └── product.ts activity.ts location.ts assistant.ts news.ts
├── utils/
│   ├── request.ts        # axios 封装：baseURL、token、拆包 Result、401 跳登录、上传
│   └── format.ts         # 时间/金额格式化等展示工具
├── stores/
│   └── user.ts           # 登录态（token + 用户信息，持久化 localStorage）
└── views/                # 页面，按模块分目录（见上面的对照表）
```

## 怎么加一个新页面（30 秒版）

1. `src/api/你的功能.ts`：照抄 `api/news.ts`，写接口函数和类型；
2. `src/views/你的功能/Xxx.vue`：照抄 `views/` 下最像的页面（读列表抄 news、写表单抄 admin/repair）；
3. `src/router/index.ts`：加一条路由（需要登录就加 `meta.requiresAuth: true`）；
4. 需要进顶部导航时，在 `App.vue` 的 `el-menu` 里加一个 `el-menu-item`。

完整说明（含后端、小程序端步骤）见仓库根目录 `docs/新增功能指南.md`。
