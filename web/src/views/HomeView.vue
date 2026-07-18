<template>
  <div class="home-page">
    <el-row :gutter="24" class="home-content">
      <!-- ===== 左侧边栏 ===== -->
      <el-col :xs="24" :sm="8" :md="7" :lg="6">
        <div class="sidebar">
          <!-- 用户信息卡片 -->
          <el-card class="user-card" shadow="hover">
            <div class="user-info">
              <el-avatar :size="72" :src="userStore.avatar || defaultAvatar" class="user-avatar">
                {{ userStore.name?.[0] }}
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ userStore.name }}</div>
                <div class="user-role">
                  <el-tag :type="getRoleTagType(userStore.role)" size="small">
                    {{ roleMap[userStore.role] || '访客' }}
                  </el-tag>
                </div>
                <div class="user-id">{{ userStore.profile?.studentId || userStore.profile?.username || '未认证' }}</div>
              </div>
            </div>
            <el-divider />
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-number">{{ userStore.profile?.department || '-' }}</span>
                <span class="stat-label">院系</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userStore.profile?.major || '-' }}</span>
                <span class="stat-label">专业</span>
              </div>
            </div>
          </el-card>

          <!-- 快捷功能导航 -->
          <el-card class="nav-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">🚀 快捷导航</span>
              </div>
            </template>
            <div class="nav-grid">
              <div
                v-for="item in quickNavItems"
                :key="item.path"
                class="nav-item"
                @click="goTo(item.path)"
              >
                <div class="nav-icon" :style="{ backgroundColor: item.color }">
                  <el-icon :size="28"><component :is="item.icon" /></el-icon>
                </div>
                <span class="nav-label">{{ item.label }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>

      <!-- ===== 右侧主内容 ===== -->
      <el-col :xs="24" :sm="16" :md="17" :lg="18">
        <el-card class="main-content" shadow="hover">
          <div class="news-section">
            <div class="section-header">
              <span class="section-title">📰 新闻资讯</span>
              <el-button type="text" @click="goTo('/news')">查看更多 →</el-button>
            </div>

            <!-- 搜索栏 -->
            <div class="search-bar">
              <el-input
                v-model="query.keyword"
                placeholder="搜索新闻标题…"
                clearable
                class="search-input"
                size="large"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
              <el-select
                v-model="query.categoryId"
                placeholder="全部分类"
                clearable
                class="search-select"
                size="large"
                @change="handleSearch"
              >
                <el-option
                  v-for="c in categories"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
              <el-button type="primary" size="large" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </div>

            <!-- 新闻列表 -->
            <div v-loading="loading" class="news-list">
              <el-empty v-if="!loading && !newsList.length" description="暂无新闻" />

              <div
                v-for="item in newsList"
                :key="item.id"
                class="news-item"
                @click="goTo(`/news/${item.id}`)"
              >
                <div class="news-cover">
                  <el-image :src="item.coverImage" fit="cover">
                    <template #placeholder>
                      <div class="image-placeholder">📰</div>
                    </template>
                    <template #error>
                      <div class="image-placeholder">📰</div>
                    </template>
                  </el-image>
                  <div v-if="item.isTop" class="top-badge">置顶</div>
                </div>
                <div class="news-info">
                  <div class="news-title-row">
                    <span class="news-title">{{ item.title }}</span>
                    <el-tag size="small" :type="getCategoryTagType(item.categoryId)">
                      {{ item.category }}
                    </el-tag>
                  </div>
                  <p class="news-summary">{{ item.summary || '暂无摘要' }}</p>
                  <div class="news-meta">
                    <span>👁️ {{ item.viewCount }}</span>
                    <span>📅 {{ formatDateTime(item.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="query.page"
                v-model:page-size="query.size"
                :total="total"
                :page-sizes="[5, 10, 20]"
                layout="total, sizes, prev, pager, next"
                background
                @size-change="fetchNews"
                @current-change="fetchNews"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import {
  Document,
  Bell,
  Reading,
  DataAnalysis,
  Calendar,
  Wallet,
  CreditCard,
  Cpu,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getNewsList, getNewsCategories } from '@/api/news'
import type { NewsListItem, NewsCategory } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// ===== 用户角色映射 =====
const roleMap: Record<string, string> = {
  student: '学生',
  teacher: '教师',
  admin: '管理员',
  guest: '访客',
}

function getRoleTagType(role: string) {
  const map: Record<string, 'primary' | 'success' | 'danger' | 'info'> = {
    student: 'primary',
    teacher: 'success',
    admin: 'danger',
    guest: 'info',
  }
  return map[role] || 'info'
}

function getCategoryTagType(categoryId: number) {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info'> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info',
  }
  return map[categoryId] || 'info'
}

// ===== 快捷导航配置（8个功能，2列展示） =====
const quickNavItems = [
  { path: '/news', label: '新闻资讯', icon: Document, color: '#409EFF' },
  { path: '/notice', label: '公告通知', icon: Bell, color: '#E6A23C' },
  { path: '/schedule', label: '课程管理', icon: Reading, color: '#67C23A' },
  { path: '/scores', label: '成绩查询', icon: DataAnalysis, color: '#909399' },
  { path: '/exam', label: '考试安排', icon: Calendar, color: '#F56C6C' },
  { path: '/payments', label: '校园缴费', icon: Wallet, color: '#9B59B6' },
  { path: '/card', label: '校园卡', icon: CreditCard, color: '#1ABC9C' },
  { path: '/ai-chat', label: 'AI助手', icon: Cpu, color: '#3498DB' },
]

// ===== 新闻列表状态 =====
const loading = ref(false)
const newsList = ref<NewsListItem[]>([])
const categories = ref<NewsCategory[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 5,
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

async function fetchNews() {
  loading.value = true
  try {
    const res = await getNewsList({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId,
    })
    if (res.code === 200 && res.data) {
      newsList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchNews()
}

function goTo(path: string) {
  router.push(path)
}

onMounted(() => {
  fetchCategories()
  fetchNews()
})
</script>

<style scoped>
.home-page {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.home-content {
  max-width: 1500px;
  margin: 0 auto;
}

/* ===== 左侧边栏 ===== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 用户卡片 ===== */
.user-card {
  border-radius: 12px;
  overflow: hidden;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px 0;
}

.user-avatar {
  flex-shrink: 0;
  border: 3px solid #f0f2f5;
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-role {
  margin: 2px 0;
}

.user-id {
  font-size: 13px;
  color: #909399;
}

.user-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-number {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* ===== 快捷导航卡片 ===== */
.nav-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  background: #f5f7fa;
  border: 1px solid transparent;
}

.nav-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.nav-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.nav-label {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  text-align: center;
  white-space: nowrap;
}

/* ===== 主内容 ===== */
.main-content {
  border-radius: 12px;
  overflow: hidden;
  min-height: 500px;
}

/* ===== 新闻区域 ===== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* ===== 搜索栏 ===== */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.search-select {
  width: 160px;
}

/* ===== 新闻列表 ===== */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 300px;
}

.news-item {
  display: flex;
  gap: 14px;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.news-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.news-cover {
  position: relative;
  flex-shrink: 0;
  width: 140px;
  height: 96px;
  border-radius: 6px;
  overflow: hidden;
}

.news-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  background: #f0f2f5;
}

.top-badge {
  position: absolute;
  top: 4px;
  left: 4px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 3px;
}

.news-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.news-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.news-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-title-row .el-tag {
  flex-shrink: 0;
}

.news-summary {
  margin: 4px 0;
  color: #606266;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.news-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ===== 响应式 ===== */
@media (max-width: 992px) {
  .nav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 10px;
  }
  .user-info {
    flex-direction: column;
    text-align: center;
  }
  .user-stats {
    flex-direction: column;
    gap: 6px;
  }
  .nav-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  .nav-item {
    padding: 10px 4px;
  }
  .nav-icon {
    width: 40px;
    height: 40px;
  }
  .nav-label {
    font-size: 11px;
  }
  .news-item {
    flex-direction: column;
  }
  .news-cover {
    width: 100%;
    height: 140px;
  }
  .search-bar {
    flex-direction: column;
  }
  .search-input,
  .search-select {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .nav-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>