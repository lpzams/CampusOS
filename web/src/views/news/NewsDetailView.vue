<script setup lang="ts">
/**
 * 新闻详情页 —— 「带路由参数的读页面」示例。
 *
 * URL 形如 /news/123，123 通过 route.params.id 拿到。
 * 对应后端接口：GET /api/news/{id}（后端会顺带把浏览量 +1）。
 * 收藏功能整合自 CampusOS_a：右上角星标收藏/取消，列表见「我的收藏」。
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { favoriteNews, getFavorites, getNewsDetail, unfavoriteNews } from '@/api/news'
import type { NewsItem } from '@/api/news'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const news = ref<NewsItem | null>(null)

// ===== 收藏状态 =====
const favorited = ref(false)
const favoriteBusy = ref(false)

async function loadFavoriteState(id: number) {
  if (!userStore.isLoggedIn) return
  try {
    const favorites = await getFavorites()
    favorited.value = favorites.some(item => item.id === id)
  } catch {
    // 收藏状态拉不到不影响正文阅读
  }
}

async function toggleFavorite() {
  if (!news.value || favoriteBusy.value) return
  favoriteBusy.value = true
  try {
    if (favorited.value) {
      await unfavoriteNews(news.value.id)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteNews(news.value.id)
      favorited.value = true
      ElMessage.success('已加入我的收藏')
    }
  } finally {
    favoriteBusy.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    news.value = await getNewsDetail(route.params.id as string)
    if (news.value) loadFavoriteState(news.value.id)
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
      <div class="title-row">
        <h1 class="detail-title">{{ news.title }}</h1>
        <el-button
          :type="favorited ? 'warning' : 'default'"
          :icon="favorited ? StarFilled : Star"
          :loading="favoriteBusy"
          round
          @click="toggleFavorite"
        >
          {{ favorited ? '已收藏' : '收藏' }}
        </el-button>
      </div>
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

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
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
