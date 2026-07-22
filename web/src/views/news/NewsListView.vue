<script setup lang="ts">
/**
 * 新闻列表页 —— 「读」页面的标准写法示例。
 *
 * 流程：onMounted 拉第一页 -> 用户改搜索条件/翻页 -> 重新调 pageNews()。
 * 对应后端接口：GET /api/news（只返回已发布的新闻）。
 *
 * 【新增功能时】做列表页照抄本文件：换成你的 api 函数、字段和筛选条件即可。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NEWS_CATEGORIES, pageNews } from '@/api/news'
import type { NewsItem } from '@/api/news'
import { formatDateTime, summary } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref<NewsItem[]>([])
const total = ref(0)

// 查询条件：pageNum/pageSize 对应后端 PageQuery，keyword/category 对应 NewsPageQuery。
// 空字符串表示"不筛选"，后端对空白条件会自动忽略。
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
})

/** 栏目 -> 彩色标签样式（与桌面端暮紫主题呼应） */
const CATEGORY_CLASS: Record<string, string> = {
  校园新闻: 'cat-violet',
  学院动态: 'cat-teal',
  通知公告: 'cat-rose',
  政策文件: 'cat-gold',
}

async function fetchList() {
  loading.value = true
  try {
    const page = await pageNews(query)
    list.value = page.list
    total.value = page.total
  } finally {
    // 无论成功失败都要关 loading（错误提示已由 request.ts 统一弹出）
    loading.value = false
  }
}

/** 搜索条件变化：回到第一页再查，否则可能停在一个不存在的页码上 */
function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function goDetail(id: number) {
  router.push(`/news/${id}`)
}

onMounted(fetchList)
</script>

<template>
  <div>
    <!-- 报刊头版式 hero -->
    <section class="news-hero">
      <span class="hero-eyebrow">CAMPUS DAILY · 校园资讯</span>
      <h1>让校园里的每一条消息<em>准时抵达</em></h1>
      <p>聚合新闻、学院动态、通知公告与政策文件，共 {{ total }} 篇</p>
    </section>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-bar">
      <div class="search-form">
        <el-input
          v-model="query.keyword"
          placeholder="搜索新闻标题…"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="query.category"
          placeholder="全部栏目"
          clearable
          class="search-select"
          @change="handleSearch"
        >
          <el-option
            v-for="c in NEWS_CATEGORIES"
            :key="c"
            :label="c"
            :value="c"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 新闻列表 -->
    <div v-loading="loading" class="news-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无新闻，可以去「资讯管理」发布一条试试" />

      <el-card
        v-for="item in list"
        :key="item.id"
        shadow="never"
        class="news-card"
        @click="goDetail(item.id)"
      >
        <div class="news-title-row">
          <span class="news-title">{{ item.title }}</span>
          <span class="news-cat" :class="CATEGORY_CLASS[item.category] || 'cat-violet'">{{ item.category }}</span>
        </div>
        <p class="news-summary">{{ summary(item.content) }}</p>
        <div class="news-meta">
          <span>{{ item.author }}</span>
          <span>{{ formatDateTime(item.publishedAt) }}</span>
          <span>{{ item.viewCount }} 次浏览</span>
        </div>
      </el-card>
    </div>

    <!-- 分页器：total 由后端返回，页码/条数变化时重新查询 -->
    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, prev, pager, next, sizes"
        background
        @current-change="fetchList"
        @size-change="handleSearch"
      />
    </div>
  </div>
</template>

<style scoped>
.news-hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
  padding: 34px 36px 30px;
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(120deg, #2b1b5e 0%, #6b3fa0 55%, #c06fae 100%);
  box-shadow: 0 16px 40px #6b3fa036;
}

/* 星点装饰 */
.news-hero::after {
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

.news-hero h1 {
  margin: 10px 0 8px;
  font: 700 30px/1.35 Georgia, "STSong", serif;
}

.news-hero h1 em {
  color: #ffd98a;
  font-style: normal;
}

.news-hero p {
  margin: 0;
  color: #ffffffb3;
  font-size: 13px;
}

.search-bar {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  gap: 12px;
}

.search-input {
  flex: 1;
}

.search-select {
  width: 160px;
}

.news-list {
  min-height: 200px;
}

.news-card {
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform .18s, box-shadow .18s;
}

.news-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 34px #7c5cd626;
}

.news-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.news-title {
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

.news-summary {
  margin: 8px 0;
  color: #6a628c;
  font-size: 14px;
  line-height: 1.6;
}

.news-meta {
  display: flex;
  gap: 16px;
  color: #a89ec9;
  font-size: 13px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

@media (max-width: 640px) {
  .news-hero { padding: 24px 20px; }
  .news-hero h1 { font-size: 22px; }
}
</style>
