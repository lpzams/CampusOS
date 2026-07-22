<div align="center">
  <img src="docs/campusos-hero.svg" alt="CampusOS" width="100%" />
  <p><strong>A modular campus service platform for web, mini-program and API clients.</strong></p>
  <p><a href="README.zh-CN.md">中文文档</a> · <a href="https://github.com/lpzams/CampusOS/issues">Issues</a> · <a href="https://github.com/lpzams/CampusOS">Repository</a></p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3" />
  <img src="https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white" alt="Vue 3" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker Compose" />
</div>

## Overview

CampusOS brings common university workflows into one platform: authentication, teaching, grades, exams, news, notices, activities, dormitory services, campus cards, payments, repairs, marketplace, maps and an optional AI assistant.

The repository contains a Spring Boot API, a Vue 3 web client, a uni-app mini-program client, SQL migrations and crawler/data tooling. Shared REST APIs keep the clients consistent while the backend is organized into domain, application, infrastructure and API layers.

## Architecture

```text
Web / Mini-program → REST API · unified Result · JWT
                              ↓
             Spring Boot · domain / application / infrastructure / API
                              ↓
                         MySQL 8 · Redis
```

| Component | Location | Purpose |
| --- | --- | --- |
| Backend | [`backend/`](backend) | Java 17, Spring Boot 3, MyBatis-Plus, Maven multi-module API |
| Web | [`web/`](web) | Vue 3, TypeScript, Vite, Element Plus, Pinia |
| Mini-program | [`miniapp/`](miniapp) | uni-app + Vue 3 client for WeChat |
| Data tools | [`web-crawler/`](web-crawler) | Crawler, HAR analysis and MySQL seed export |
| Database | [`docs/sql/`](docs/sql) | Initialization, incremental migrations and demo data |

## Quick start

```bash
git clone https://github.com/lpzams/CampusOS.git
cd CampusOS
cp .env.example .env
docker compose up -d --build
```

| Service | URL |
| --- | --- |
| Web | http://localhost:8081 |
| API | http://localhost:8080 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

The first startup applies SQL files under `docs/sql/`. The demo account is `admin / 123456`; change it before using the system outside a local demo.

For local development, start MySQL and Redis with Docker, run `mvn spring-boot:run -pl campus-api` from `backend/`, then run `npm install && npm run dev` from `web/`. Import `miniapp/` with HBuilderX or WeChat Developer Tools.

## Configuration and security

Copy `.env.example` to `.env` for local settings. Never commit real API keys, passwords or certificates. Before production, replace the demo database password and `CAMPUS_JWT_SECRET`, and configure AI credentials through environment variables only.

## Checks

```bash
cd web && npm run build
cd miniapp && node --test tests/*.test.mjs
cd backend && mvn test
```

## Documentation

- [`docs/贡献指南.md`](docs/贡献指南.md) · Contribution guide
- [`docs/新增功能指南.md`](docs/新增功能指南.md) · Feature development guide
- [`docs/sql/README.md`](docs/sql/README.md) · Database scripts
- [`接口文档.md`](接口文档.md) · API reference

## License

No formal open-source license has been declared. Contact the maintainers before commercial redistribution.
