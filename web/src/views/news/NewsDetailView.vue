<script setup lang="ts">
/**
 * 新闻详情页 —— 「带路由参数的读页面」示例。
 *
 * URL 形如 /news/123，123 通过 route.params.id 拿到。
 * 对应后端接口：GET /api/news/{id}（后端会顺带把浏览量 +1）。
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNewsDetail } from '@/api/news'
import type { NewsItem } from '@/api/news'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const news = ref<NewsItem | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    news.value = await getNewsDetail(route.params.id as string)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <!-- 返回上一页（通常是列表页） -->
    <el-page-header content="新闻详情" class="page-header" @back="router.back()" />

    <el-card v-if="news" shadow="never">
      <h1 class="detail-title">{{ news.title }}</h1>
      <div class="detail-meta">
        <el-tag size="small">{{ news.category }}</el-tag>
        <span>{{ news.author }}</span>
        <span>发布于 {{ formatDateTime(news.publishedAt) }}</span>
        <span>{{ news.viewCount }} 次浏览</span>
      </div>
      <el-divider />
      <!-- 正文是纯文本，pre-wrap 保留后台录入时的换行 -->
      <div class="detail-content">{{ news.content }}</div>
    </el-card>

    <!-- 接口报错（如 id 不存在）时 news 为空，给个兜底展示 -->
    <el-empty v-else-if="!loading" description="新闻不存在或已下线" />
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.detail-title {
  margin: 0 0 12px;
  font-size: 22px;
  color: #303133;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #909399;
  font-size: 13px;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}
</style>
