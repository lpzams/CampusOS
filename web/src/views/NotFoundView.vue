<template>
  <div class="not-found-page">
    <div class="not-found-container">
      <!-- 404 图标 -->
      <div class="not-found-icon">🔍</div>

      <!-- 状态码 -->
      <h1 class="not-found-code">404</h1>

      <!-- 提示信息 -->
      <h2 class="not-found-title">页面不见了</h2>
      <p class="not-found-desc">
        抱歉，您访问的页面不存在或已被移除<br />
        请检查网址是否正确，或返回首页继续浏览
      </p>

      <!-- 操作按钮 -->
      <div class="not-found-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
      </div>

      <!-- 推荐链接 -->
      <div class="not-found-links">
        <span>你可能想去：</span>
        <el-link type="primary" @click="goTo('/news')">新闻资讯</el-link>
        <el-link type="primary" @click="goTo('/notice')">公告通知</el-link>
        <el-link type="primary" @click="goTo('/activities')">校园活动</el-link>
        <el-link type="primary" @click="goTo('/products')">二手交易</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

function goHome() {
  router.push('/')
}

function goBack() {
  router.back()
}

function goTo(path: string) {
  router.push(path)
}
</script>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 20px;
}

.not-found-container {
  text-align: center;
  max-width: 600px;
  padding: 40px 50px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}

.not-found-icon {
  font-size: 72px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-16px);
  }
}

.not-found-code {
  font-size: 80px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.not-found-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.not-found-desc {
  font-size: 15px;
  color: #909399;
  margin: 0 0 30px 0;
  line-height: 1.8;
}

.not-found-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.not-found-actions .el-button {
  padding: 10px 28px;
  font-size: 15px;
}

.not-found-links {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
  color: #606266;
}

.not-found-links .el-link {
  font-size: 14px;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .not-found-container {
    padding: 24px 20px;
  }
  .not-found-code {
    font-size: 56px;
  }
  .not-found-title {
    font-size: 20px;
  }
  .not-found-icon {
    font-size: 52px;
  }
  .not-found-actions .el-button {
    width: 100%;
  }
  .not-found-links {
    gap: 8px;
  }
}
</style>