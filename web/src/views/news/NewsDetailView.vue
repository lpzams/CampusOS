<script setup lang="ts">
/**
 * 新闻详情页 —— 展示新闻完整内容。
 * 对应后端接口：GET /news/detail/{id}
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNewsDetail } from '@/api/news'
import type { NewsDetail } from '@/api/types'
import { formatDateTime } from '@/utils/format'  // ✅ 改1：formatDate → formatDateTime

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const news = ref<NewsDetail | null>(null)

onMounted(async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    // ✅ 改2：String(id) 确保类型正确
    const res = await getNewsDetail(String(id))
    if (res.code === 200 && res.data) {
      news.value = res.data
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <el-page-header content="新闻详情" class="page-header" @back="router.back()" />

    <el-card v-if="news" shadow="never">
      <h1 class="detail-title">{{ news.title }}</h1>
      <div class="detail-meta">
        <el-tag size="small">{{ news.category }}</el-tag>
        <span>✍️ {{ news.author }}</span>
        <span>👁️ {{ news.viewCount }}</span>
        <span>❤️ {{ news.favoriteCount }}</span>
        <!-- ✅ 改3：formatDate → formatDateTime -->
        <span>📅 {{ formatDateTime(news.createTime) }}</span>
      </div>
      <el-divider />
      <div class="detail-content" v-html="news.content" />
    </el-card>

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
  flex-wrap: wrap;
}
.detail-content {
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}
.detail-content :deep(p) {
  margin: 0 0 12px 0;
}
.detail-content :deep(img) {
  max-width: 100%;
}
</style>