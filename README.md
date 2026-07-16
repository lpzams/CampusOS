# CampusOS —— 高校智慧校园门户系统

课程实践项目：一套「**Spring Boot 后端 + Vue3 网站 + 微信小程序**」三端同构的校园门户，
规划 15 个功能模块（见 [项目15个简陋功能.md](./项目15个简陋功能.md)）。
**校园新闻模块已作为全链路示例实现**，其余模块由团队成员照着它扩展。

```
             用户
        Web网站     微信小程序
          (web/)     (miniapp/)
             \          /
              REST API（JSON，统一 Result 结构）
                   |
          Spring Boot 后端 (backend/，DDD 洋葱架构)
                   |
              MySQL (+ Redis 预留)
```

## 技术栈

| 端 | 技术 | 目录 |
| --- | --- | --- |
| 后端 | Java 17 · Spring Boot 3.2 · MyBatis-Plus · MySQL（Maven 多模块，DDD 分层） | [backend/](./backend) |
| 网站 | Vue 3 · TypeScript · Vite · Element Plus · Pinia · Vue Router | [web/](./web) |
| 小程序 | uni-app（Vue 3）· 微信小程序 | [miniapp/](./miniapp) |

## 快速开始

> 前置：JDK 17+、Maven 3.8+、Node 18+、MySQL 8、微信开发者工具（跑小程序才需要）。

```bash
# 1. 初始化数据库（建库 campus_os + 新闻表 + 示例数据）
mysql -uroot -p < docs/sql/init.sql

# 2. 启动后端（默认 8080；数据库账号密码在 backend/campus-api/src/main/resources/application.yml）
cd backend
mvn spring-boot:run -pl campus-api
#    验证：浏览器打开 http://localhost:8080/api/news 能看到 JSON

# 3. 启动网站端（默认 5173，/api 由 vite 代理转发到后端）
cd web
npm install
npm run dev

# 4. 小程序端：用 HBuilderX 导入 miniapp/ 目录，运行到微信开发者工具
#    详细步骤见 miniapp/README.md
```

## 目录结构

```
CampusOS/
├── backend/            # Spring Boot 后端（campus-common/domain/application/infrastructure/api 五模块）
├── web/                # Vue3 网站端（含开发说明 web/README.md）
├── miniapp/            # uni-app 微信小程序端（含开发说明 miniapp/README.md）
├── docs/
│   ├── sql/init.sql    # 数据库初始化脚本
│   ├── 新增功能指南.md   # ★ 加功能的 step-by-step（照 news 模块抄）
│   └── 贡献指南.md      # 分支/提交/代码规范
├── 工程文档-指导版/      # 课程要求的工程文档（需求规约、测试计划等）
└── 项目15个简陋功能.md   # 15 个功能模块的需求描述
```

## 功能模块进度

| # | 模块 | 状态 |
| --- | --- | --- |
| 3 | 校园新闻资讯 | ✅ 全链路示例（后端 + 网站浏览/管理 + 小程序浏览） |
| 1 | 用户登录与统一认证 | ⬜ 待认领（后端已预留 JWT 依赖、前端已预留 token 位置） |
| 2 | 个人信息中心 | ⬜ 待认领 |
| 4 | 校园公告通知 | ⬜ 待认领（与新闻最像，适合第一个动手） |
| 5-15 | 课程/成绩/考试/缴费/校园卡/宿舍/报修/二手/活动/地图/AI 助手 | ⬜ 待认领 |

## 团队开发怎么开始

1. 读 [docs/贡献指南.md](./docs/贡献指南.md)：分支怎么拉、提交怎么写；
2. 认领一个模块，照 [docs/新增功能指南.md](./docs/新增功能指南.md) 从建表一路做到小程序；
3. 卡住时看 news 模块对应文件 —— 每个文件头部的注释都写了"新增功能时怎么抄"。
