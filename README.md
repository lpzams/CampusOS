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

> 前置：装好 [Docker Desktop](https://www.docker.com/products/docker-desktop/)。
> 用「开发模式」另需 JDK 17+、Maven 3.8+、Node 18+；跑小程序需 HBuilderX + 微信开发者工具。

### 方式一：Docker 一键跑全套（汇报演示 / 快速体验）

```bash
docker compose up -d --build
```

首次会拉镜像+下载依赖（几分钟），完成后：

| 访问 | 地址 |
| --- | --- |
| 网站 | http://localhost:8081 |
| 后端接口 | http://localhost:8080/api/news |
| MySQL / Redis | localhost:3306（root/root）/ localhost:6379 |

数据库首次启动自动执行 `docs/sql/` 全部脚本（建库建表 + 示例数据）。
首次启动会创建演示管理员 `admin / 123456`；部署前请登录修改密码，并设置
`CAMPUS_JWT_SECRET` 与 `CAMPUS_EXPOSE_SMS_CODE=false`。
改了代码想更新容器：还是这一条命令。更多命令见 `docker-compose.yml` 头部注释。

### 方式二：开发模式（平时写代码）

```bash
# 1. Docker 只跑数据库（容器后端/网站不启动，8080 留给 IDEA）
docker compose up -d mysql redis

# 2. 后端在 IDEA 里跑 CampusApplication，或命令行：
cd backend
mvn spring-boot:run -pl campus-api
#    验证：浏览器打开 http://localhost:8080/api/news 能看到 JSON

# 3. 网站端热更新开发（默认 5173，/api 由 vite 代理转发到后端）
cd web
npm install
npm run dev

# 4. 小程序端：用 HBuilderX 导入 miniapp/ 目录，运行到微信开发者工具
#    详细步骤见 miniapp/README.md
```

> 两种方式别同时开：容器后端和 IDEA 后端都占 8080 端口，会冲突
> （`docker compose stop backend web` 可只停这两个容器）。
> 没装 Docker 的同学：本机 MySQL 手动导入 `mysql -uroot -p < docs/sql/001_init.sql` 也行。

## 目录结构

> ★ 标记的文件/目录是加新功能时的「模板」，照抄即可；标了「必改」的是加功能时一定会碰的文件。
> 更细的分端说明见 [web/README.md](./web/README.md) 和 [miniapp/README.md](./miniapp/README.md)。

```
CampusOS/
│
├── backend/                       # ===== Spring Boot 后端（Maven 多模块，DDD 洋葱架构）=====
│   ├── pom.xml                    # 父 POM：统一管依赖版本，声明下面五个子模块
│   ├── Dockerfile                 # 后端镜像：Maven 打包 → JRE 运行（docker compose 用）
│   ├── docker/                    # 容器构建配套（Maven 阿里云镜像配置）
│   │
│   ├── campus-common/             # 〔公共层〕全后端共享的通用代码，不含任何业务
│   │   └── …/common/
│   │       ├── api/               #   Result 统一返回 / PageResult 统一分页 / ResultCode 错误码
│   │       ├── exception/         #   BusinessException：业务可预期错误统一抛它
│   │       └── query/             #   PageQuery：分页参数基类（pageNum/pageSize）
│   │
│   ├── campus-domain/             # 〔领域层·最内层〕纯业务对象，不 import 任何框架
│   │   └── …/domain/news/         #   News 实体（业务规则写在实体方法里，如 publish()）
│   │                              #   NewsRepository 仓储接口（实现在 infrastructure）★模板
│   │
│   ├── campus-application/        # 〔应用层〕用例编排：一个功能一个 AppService
│   │   └── …/application/news/    #   NewsAppService（编排+事务）★模板
│   │       ├── dto/               #   NewsDTO：返回给前端的数据结构
│   │       ├── command/           #   CreateNewsCommand：写操作入参（@NotBlank 校验写这里）
│   │       └── query/             #   NewsPageQuery：查询入参（extends PageQuery）
│   │
│   ├── campus-infrastructure/     # 〔基础设施层〕数据库落地（MyBatis-Plus）
│   │   └── …/persistence/news/    #   NewsPO（表 t_news 的映射）/ NewsMapper / NewsConverter（PO↔实体）
│   │                              #   NewsRepositoryImpl（实现领域层的仓储接口）★模板
│   │
│   └── campus-api/                # 〔接口层·最外层〕HTTP 入口 + 装配，唯一可运行的模块
│       └── src/main/
│           ├── java/…/api/
│           │   ├── CampusApplication.java   # 启动类（main 在这，跑后端就是跑它）
│           │   ├── controller/news/         # NewsController：REST 接口，只转发不写业务 ★模板
│           │   ├── common/                  # GlobalExceptionHandler：全局异常 → 统一 Result
│           │   └── config/                  # MybatisPlusConfig 等框架配置
│           └── resources/application.yml    # 端口 8080、MySQL/Redis 连接（数据库密码改这里）
│
├── web/                           # ===== Vue3 网站端（Vite + TypeScript + Element Plus）=====
│   ├── package.json               # 依赖与脚本：npm run dev（开发）/ npm run build（打包）
│   ├── vite.config.ts             # @ 别名、Element Plus 按需导入、/api 代理转发到后端 8080
│   ├── Dockerfile                 # 网站镜像：npm build → nginx 托管（docker compose 用）
│   ├── docker/nginx.conf          # 容器里的 nginx：SPA 路由回退 + /api 反代给后端
│   ├── .env.development           # 开发环境变量（API 基础路径 /api，走代理免跨域）
│   ├── .env.production            # 生产环境变量（上线时改成真实后端地址）
│   ├── index.html                 # 单页应用入口 HTML
│   ├── public/                    # 原样拷贝的静态资源（favicon 等）
│   └── src/
│       ├── main.ts                # 入口：装配 Pinia / Router / Element Plus
│       ├── App.vue                # 全站布局：顶栏导航 + 内容区 + 页脚（加导航菜单改这里）
│       ├── router/index.ts        # 路由表（加页面必改）
│       ├── api/                   # ★ 所有后端接口调用只写在这里（一个功能一个文件）
│       │   ├── types.ts           #   Result/PageResult：与后端约定的通用类型
│       │   └── news.ts            #   新闻模块接口 + 类型 ★模板
│       ├── utils/
│       │   ├── request.ts         #   axios 封装：统一拆包 Result、统一错误提示（一般不用动）
│       │   └── format.ts          #   时间格式化等展示小工具
│       ├── stores/
│       │   └── user.ts            #   Pinia 用户状态（登录模块完成后接入真实数据）
│       └── views/                 # 页面组件，按功能分目录
│           ├── news/              #   NewsListView 列表（搜索+分页）/ NewsDetailView 详情 ★模板
│           └── admin/             #   NewsManageView 后台管理（发布/下线/删除）★模板
│
├── miniapp/                       # ===== 微信小程序端（uni-app Vue3，HBuilderX 导入即用）=====
│   ├── main.js                    # 入口（uni-app 固定写法，不用动）
│   ├── App.vue                    # 应用生命周期 + 全局样式
│   ├── manifest.json              # 应用配置：appid、微信平台设置（urlCheck 已关）
│   ├── pages.json                 # 页面注册 + 导航栏样式（加页面必改，漏注册会白屏）
│   ├── uni.scss                   # 全局 SCSS 变量（改主题色在这里）
│   ├── api/
│   │   └── news.js                # 新闻模块接口 ★模板
│   ├── utils/
│   │   ├── request.js             # uni.request 封装：拆包 Result、错误 toast（BASE_URL 在这改）
│   │   └── format.js              # 时间格式化等展示小工具
│   └── pages/
│       ├── index/index.vue        # 首页：最新新闻（下拉刷新/上拉加载/栏目筛选）★列表页模板
│       └── news/detail.vue        # 新闻详情（带参数跳转）★详情页模板
│
├── docs/                          # ===== 团队文档 =====
│   ├── sql/
│   │   └── 001_init.sql           # 建库 campus_os + t_news + 示例数据；新表按 002_功能.sql 递增追加
│   ├── 新增功能指南.md             # ★ 加功能 step-by-step：建表 → 后端四层 → 网站端 → 小程序端
│   └── 贡献指南.md                 # 分支/提交规范、各层职责红线、提 PR 前自检
│
├── docker-compose.yml             # ★ 一键全套：MySQL+Redis+后端+网站（docker compose up -d --build）
├── 工程文档-指导版/                 # 课程要求的工程文档（需求规约、测试计划、答辩 PPT 等）
├── 项目15个简陋功能.md              # 15 个功能模块的需求描述（做什么以这份为准）
├── .gitignore                     # 忽略 node_modules/target/unpackage 等生成物
└── README.md                      # 本文件
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
