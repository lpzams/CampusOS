<script setup lang="ts">
/**
 * 我的收藏（整合自 CampusOS_a 的 FavoriteView）。
 *
 * 对应后端 GET /api/news/favorites（返回收藏的完整新闻列表，需登录）。
 * 支持在列表里直接取消收藏；点击卡片进入新闻详情。
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { getFavorites, unfavoriteNews } from '@/api/news'
import type { NewsItem } from '@/api/news'
import { formatDateTime, summary } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref<NewsItem[]>([])

/** 栏目 -> 彩色标签样式（与新闻列表页一致） */
const CATEGORY_CLASS: Record<string, string> = {
  校园新闻: 'cat-violet',
  学院动态: 'cat-teal',
  通知公告: 'cat-rose',
  政策文件: 'cat-gold',
}

async function fetchFavorites() {
  loading.value = true
  try {
    list.value = await getFavorites()
  } finally {
    loading.value = false
  }
}

/** 取消收藏后就地移除，不整页刷新 */
async function removeFavorite(item: NewsItem, event: MouseEvent) {
  event.stopPropagation()
  await unfavoriteNews(item.id)
  list.value = list.value.filter(row => row.id !== item.id)
  ElMessage.success('已取消收藏')
}

function goDetail(id: number) {
  router.push(`/news/${id}`)
}

onMounted(fetchFavorites)
</script>

<template>
  <div>
    <!-- 收藏夹 hero（沿用全站暮紫渐变风格） -->
    <section class="fav-hero">
      <span class="hero-eyebrow">MY FAVORITES · 我的收藏</span>
      <h1>把值得反复看的资讯<em>收进口袋</em></h1>
      <p>共收藏 {{ list.length }} 篇资讯</p>
    </section>

    <div v-loading="loading" class="fav-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无收藏，去资讯详情页点亮 ☆ 试试" />

      <el-card
        v-for="item in list"
        :key="item.id"
        shadow="never"
        class="fav-card"
        @click="goDetail(item.id)"
      >
        <div class="fav-title-row">
          <span class="fav-title">{{ item.title }}</span>
          <span class="news-cat" :class="CATEGORY_CLASS[item.category] || 'cat-violet'">{{ item.category }}</span>
        </div>
        <p class="fav-summary">{{ summary(item.content) }}</p>
        <div class="fav-footer">
          <div class="fav-meta">
            <span>✍️ {{ item.author }}</span>
            <span>👁️ {{ item.viewCount }}</span>
            <span>📅 {{ formatDateTime(item.publishedAt || item.createdAt) }}</span>
          </div>
          <el-button size="small" text type="danger" :icon="Star" @click="removeFavorite(item, $event)">
            取消收藏
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.fav-hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
  padding: 30px 36px 26px;
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(120deg, #4a2c78 0%, #7c5cd6 60%, #c06fae 100%);
  box-shadow: 0 16px 40px #6b3fa036;
}

.fav-hero::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: radial-gradient(#ffffff59 0 1.4px, transparent 2.2px), radial-gradient(#ffe9ad4d 0 1px, transparent 1.8px);
  background-size: 180px 130px, 240px 170px;
}

.hero-eyebrow {
  color: #ffd98a;
  font: 700 11px monospace;
  letter-spacing: 3px;
}

.fav-hero h1 {
  margin: 10px 0 8px;
  font: 700 26px/1.35 Georgia, "STSong", serif;
}

.fav-hero h1 em {
  color: #ffd98a;
  font-style: normal;
}

.fav-hero p {
  margin: 0;
  color: #ffffffb3;
  font-size: 13px;
}

.fav-list {
  min-height: 200px;
}

.fav-card {
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform .18s, box-shadow .18s;
}

.fav-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 34px #7c5cd626;
}

.fav-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.fav-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2350;
}

.news-cat {
  flex-shrink: 0;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.cat-violet { color: #6247ab; background: #efe9fc; }
.cat-teal { color: #1d7a72; background: #e2f6f2; }
.cat-rose { color: #b8496d; background: #fde9f0; }
.cat-gold { color: #96690f; background: #fdf3dd; }

.fav-summary {
  margin: 8px 0;
  color: #6a628c;
  font-size: 14px;
  line-height: 1.6;
}

.fav-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.fav-meta {
  display: flex;
  gap: 16px;
  color: #a89ec9;
  font-size: 13px;
}

@media (max-width: 640px) {
  .fav-hero { padding: 22px 20px; }
  .fav-hero h1 { font-size: 20px; }
}
</style>
