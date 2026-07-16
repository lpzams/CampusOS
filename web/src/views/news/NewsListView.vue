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
      <el-empty v-if="!loading && list.length === 0" description="暂无新闻，可以去「新闻管理」发布一条试试" />

      <el-card
        v-for="item in list"
        :key="item.id"
        shadow="hover"
        class="news-card"
        @click="goDetail(item.id)"
      >
        <div class="news-title-row">
          <span class="news-title">{{ item.title }}</span>
          <el-tag size="small">{{ item.category }}</el-tag>
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
  color: #303133;
}

.news-summary {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.news-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 13px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
