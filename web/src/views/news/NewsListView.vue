<script setup lang="ts">
/**
 * 新闻列表页 —— 展示所有已发布的新闻。
 * 对应后端接口：GET /news/list
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNewsList, getNewsCategories } from '@/api/news'
import type { NewsListItem, NewsCategory } from '@/api/types'
import { formatDateTime } from '@/utils/format'  // ✅ formatDate → formatDateTime

const router = useRouter()

const loading = ref(false)
const list = ref<NewsListItem[]>([])
const categories = ref<NewsCategory[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
})

async function fetchCategories() {
  try {
    const res = await getNewsCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch {
    // 分类接口失败不影响主列表
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getNewsList({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId,
    })
    if (res.code === 200 && res.data) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function goDetail(id: number) {
  router.push(`/news/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
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
          v-model="query.categoryId"
          placeholder="全部分类"
          clearable
          class="search-select"
          @change="handleSearch"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 新闻列表 -->
    <div v-loading="loading" class="news-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无新闻" />

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
        <p class="news-summary">{{ item.summary || '暂无摘要' }}</p>
        <div class="news-meta">
          <span>📂 {{ item.category }}</span>
          <span>👁️ {{ item.viewCount }}</span>
          <!-- ✅ formatDate → formatDateTime -->
          <span>📅 {{ formatDateTime(item.createTime) }}</span>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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