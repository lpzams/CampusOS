# syntax=docker/dockerfile:1
# ============================================================
# 网站端镜像 —— 多阶段构建：
#   阶段1 build：Node 里执行 npm ci + npm run build，产出 dist 静态文件
#   阶段2 运行：nginx 托管 dist，并把 /api 反向代理给后端容器
# 由根目录 docker-compose.yml 统一构建/启动，一般不用手动 docker build。
# ============================================================

FROM node:20-alpine AS build
WORKDIR /build

# npm 走国内镜像源，装依赖快很多（海外环境可删掉这行）
RUN npm config set registry https://registry.npmmirror.com

# 先只拷 package*.json 装依赖：只要依赖没变，这一层直接走缓存，改代码重建很快
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --no-audit --no-fund

# 再拷源码打包（node_modules/dist 已被 .dockerignore 排除）
COPY . .
# 容器部署时 API 走 nginx 反代的相对路径 /api。
# 环境变量优先级高于 .env.production 文件，所以这里能覆盖掉里面的占位域名。
ENV VITE_API_BASE_URL=/api
RUN npm run build

# ---------- 运行阶段 ----------
FROM nginx:1.27-alpine
ENV TZ=Asia/Shanghai

# nginx 配置：静态文件 + SPA 路由回退 + /api 反代到后端容器
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80
