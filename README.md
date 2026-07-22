# CampusOS

> A modular campus service platform with a Spring Boot API, Vue 3 web console, and uni-app mini-program client.

校园综合服务平台，提供统一的校园业务 API、Web 管理端和微信小程序端。项目采用模块化后端、统一响应模型和 Docker Compose 开发部署，适合作为校园数字化平台的课程实践、原型验证与二次开发基础。

[中文](#中文) · [English](#english) · [快速开始](#快速开始) · [Quick Start](#quick-start)

---

## 中文

### 项目概览

CampusOS 将校园常用服务聚合到同一套业务平台中：

- 账户体系：登录、注册、密码管理、JWT 会话与角色控制
- 教务服务：课程、选课、课表、成绩、考试与教学管理
- 校园生活：宿舍、水电、校园卡、缴费、报修与二手交易
- 校园资讯：新闻、公告、活动与收藏
- 智能与空间：AI 助手、校园地图、路径规划
- 数据工具：课程评价爬虫、HAR 分析、MySQL 种子数据导出

Web 端和小程序端通过 REST API 共享业务能力，后端以领域、应用、基础设施和接口层组织代码，便于按模块迭代。

### 技术栈

| 层次 | 技术 |
| --- | --- |
| API | Java 17、Spring Boot 3、MyBatis-Plus、Maven |
| Domain | DDD 分层、领域对象、Repository 抽象 |
| Web | Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router |
| Mini-program | uni-app、Vue 3、微信开发者工具 |
| Data | MySQL 8、Redis |
| Delivery | Docker、Docker Compose、Nginx |

### 仓库结构

```text
CampusOS/
├── backend/                 # Spring Boot 多模块后端
│   ├── campus-api/          # HTTP 接口与应用启动模块
│   ├── campus-application/  # 用例编排与 DTO
│   ├── campus-domain/       # 领域模型与仓储接口
│   ├── campus-infrastructure/# MyBatis-Plus、持久化与外部适配
│   └── campus-common/       # 通用响应、异常与分页模型
├── web/                     # Vue 3 + TypeScript Web 端
├── miniapp/                 # uni-app 微信小程序端
├── web-crawler/             # 课程评价采集与数据导出工具
├── docs/sql/                # 数据库初始化、增量脚本与演示数据
├── 工程文档-项目版/           # 需求、设计、测试与会议文档
├── docker-compose.yml        # MySQL、Redis、API、Web 一键编排
└── .env.example              # 环境变量模板（不含真实密钥）
```

### 快速开始

#### 方式 A：Docker Compose（推荐）

要求：Docker Desktop 4.x 或更高版本。

```bash
git clone https://github.com/lpzams/CampusOS.git
cd CampusOS
copy .env.example .env       # macOS/Linux: cp .env.example .env
docker compose up -d --build
```

服务启动后：

| 服务 | 地址 |
| --- | --- |
| Web 门户 | http://localhost:8081 |
| API | http://localhost:8080 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

首次启动会执行 `docs/sql/` 中的数据库脚本。演示环境默认账号为 `admin / 123456`，首次登录后请立即修改密码，并为生产环境设置独立的 JWT 密钥、数据库密码和 AI API Key。

常用命令：

```bash
docker compose logs -f backend
docker compose ps
docker compose stop
docker compose down
```

#### 方式 B：本地开发

要求：JDK 17+、Maven 3.8+、Node.js 18+、npm 9+；数据库可使用 Docker 启动。

```bash
# 仅启动基础设施
docker compose up -d mysql redis

# 后端
cd backend
mvn spring-boot:run -pl campus-api

# Web（新终端）
cd web
npm install
npm run dev
```

Web 开发服务器默认运行在 `http://localhost:5173`，API 通过 Vite 代理访问后端 `8080` 端口。小程序端请用 HBuilderX 或微信开发者工具导入 `miniapp/`，详细说明见 [miniapp/README.md](./miniapp/README.md)。

### 配置与安全

- `.env` 只用于本地配置，已被 `.gitignore` 忽略；提交前请确认没有真实 token、密码或证书。
- 生产环境必须替换默认数据库密码和 `CAMPUS_JWT_SECRET`。
- AI 能力通过 `ANTHROPIC_AUTH_TOKEN` 等环境变量注入，未配置时不影响基础校园业务。
- 演示数据和默认账号仅用于本地开发，不应直接用于生产环境。

### 测试与质量检查

```bash
# Web 类型检查与生产构建
cd web
npm run build

# 小程序工具函数测试
cd miniapp
node --test tests/*.test.mjs

# 后端编译与测试
cd backend
mvn test
```

### 文档与贡献

- [新增功能指南](./docs/新增功能指南.md)
- [贡献指南](./docs/贡献指南.md)
- [数据库脚本说明](./docs/sql/README.md)
- [项目接口文档](./接口文档.md)

欢迎通过 Issue 或 Pull Request 提交问题和改进建议。新增功能请保持后端、Web、小程序和数据库变更的一致性，并避免提交构建产物、依赖目录和本地配置。

### 许可证

当前仓库未声明正式开源许可证。除非获得项目维护者明确授权，请勿将代码用于商业分发。

---

## English

### Overview

CampusOS is a modular campus service platform that brings common university workflows into one system. It ships with a Spring Boot API, a Vue 3 web console, and a uni-app mini-program client.

Core capabilities include authentication and role control, teaching services, grades and exams, campus news and notices, activities, dormitory and card services, payments, repairs, second-hand trading, maps, an AI assistant, and crawler/data-export utilities.

The Web and mini-program clients share the same REST API. The backend follows a domain/application/infrastructure/API structure so features can evolve independently without duplicating business rules.

### Stack

- **Backend:** Java 17, Spring Boot 3, MyBatis-Plus, Maven, DDD-style layering
- **Web:** Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router
- **Mini-program:** uni-app, Vue 3, WeChat Developer Tools
- **Infrastructure:** MySQL 8, Redis, Docker Compose, Nginx
- **Data tooling:** Python crawler scripts, HAR analysis, MySQL seed export

### Quick Start

Requirements: Docker Desktop 4.x or later.

```bash
git clone https://github.com/lpzams/CampusOS.git
cd CampusOS
cp .env.example .env
docker compose up -d --build
```

Open `http://localhost:8081` for the Web portal and `http://localhost:8080` for the API. MySQL is exposed on `3306` and Redis on `6379`.

The first startup applies SQL files under `docs/sql/`. The demo account is `admin / 123456`; change it immediately after login and configure production secrets before deployment.

For local development, start MySQL and Redis with Docker, run `mvn spring-boot:run -pl campus-api` from `backend/`, then run `npm install && npm run dev` from `web/`. Import `miniapp/` with HBuilderX or the WeChat Developer Tools for mini-program development.

### Configuration and Security

Keep real credentials in `.env` or your deployment secret manager. Never commit API keys, passwords, certificates, or generated build output. Replace the demo database password and `CAMPUS_JWT_SECRET` before production use. The AI assistant is optional and is configured through environment variables such as `ANTHROPIC_AUTH_TOKEN`.

### Documentation

- [Feature development guide](./docs/新增功能指南.md)
- [Contribution guide](./docs/贡献指南.md)
- [Database scripts](./docs/sql/README.md)
- [API documentation](./接口文档.md)

### Contributing

Issues and pull requests are welcome. Keep backend, Web, mini-program, and database changes consistent, add a focused test for non-trivial behavior, and leave local configuration and generated artifacts out of commits.

### License

No formal open-source license has been declared for this repository. Do not redistribute the code commercially without explicit permission from the maintainers.

