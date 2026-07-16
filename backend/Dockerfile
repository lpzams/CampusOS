# syntax=docker/dockerfile:1
# ============================================================
# 后端镜像 —— 多阶段构建：
#   阶段1 build：用带 Maven+JDK17 的镜像把源码打成可执行 jar
#   阶段2 运行：只拷贝 jar 到一个精简的 JRE 镜像里跑（镜像小、干净）
# 由根目录 docker-compose.yml 统一构建/启动，一般不用手动 docker build。
# ============================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Maven 走阿里云镜像仓库，国内下载依赖快很多（海外环境可删掉这行）
COPY docker/maven-settings.xml /root/.m2/settings.xml

# 拷贝全部源码（target/.idea 等已被 .dockerignore 排除）
COPY . .

# 打包 campus-api（-am 连带构建它依赖的四个内部模块）。
# --mount=type=cache 把 Maven 本地仓库挂成缓存卷：第二次构建不再重复下载依赖。
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -q -DskipTests package -pl campus-api -am

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Shanghai

# 只把打好的 jar 拿过来（campus-api/pom.xml 里 finalName 固定为 campus-api）
COPY --from=build /build/campus-api/target/campus-api.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
