<script setup lang="ts">
/**
 * 门户首页（整合自 CampusOS_a 的 HomeView）。
 *
 * 布局：左侧「用户信息卡 + 快捷导航图标宫格」，右侧「新闻资讯搜索 + 列表」。
 * 快捷导航保留 CampusOS_a 的 8 个彩色圆角图标（新闻资讯/公告通知/课程管理/成绩查询/
 * 考试安排/校园缴费/校园卡/AI助手），路径对应本项目路由。
 * 沉浸式桌面仍在 /（CampusOS 开机动画），此页是常规门户入口。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Bell, Calendar, CreditCard, Cpu, DataAnalysis, Document, Reading, Search, Wallet,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { NEWS_CATEGORIES, pageNews } from '@/api/news'
import type { NewsItem } from '@/api/news'
import { getProfile } from '@/api/user'
import type { UserProfile } from '@/api/user'
import { fileUrl, formatDateTime, summary } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

/** 角色 -> 中文名 + 标签色 */
const ROLE_LABEL: Record<string, string> = { student: '学生', teacher: '教师', admin: '管理员', guest: '访客' }
const ROLE_TAG: Record<string, 'primary' | 'success' | 'danger' | 'info'> = {
  student: 'primary', teacher: 'success', admin: 'danger', guest: 'info',
}

// ===== 快捷导航（保留 CampusOS_a 的图标与配色） =====
const quickNavItems = [
  { path: '/news', label: '新闻资讯', icon: Document, color: '#409EFF' },
  { path: '/notice', label: '公告通知', icon: Bell, color: '#E6A23C' },
  { path: '/schedule', label: '课程管理', icon: Reading, color: '#67C23A' },
  { path: '/score', label: '成绩查询', icon: DataAnalysis, color: '#909399' },
  { path: '/exam', label: '考试安排', icon: Calendar, color: '#F56C6C' },
  { path: '/payment', label: '校园缴费', icon: Wallet, color: '#9B59B6' },
  { path: '/card', label: '校园卡', icon: CreditCard, color: '#1ABC9C' },
  { path: '/assistant', label: 'AI助手', icon: Cpu, color: '#3498DB' },
]
const visibleQuickNavItems = computed(() => quickNavItems.filter(item => userStore.canAccess(item.path)))

/** 栏目 -> 彩色标签样式（与新闻列表页一致） */
const CATEGORY_CLASS: Record<string, string> = {
  校园新闻: 'cat-violet',
  学院动态: 'cat-teal',
  通知公告: 'cat-rose',
  政策文件: 'cat-gold',
}

// ===== 个人信息卡（院系/专业/学号来自 /user/profile） =====
const profile = ref<UserProfile | null>(null)

async function loadProfile() {
  if (!userStore.isLoggedIn) return
  try {
    profile.value = await getProfile()
  } catch {
    // 卡片信息拉不到不影响首页其它区块
  }
}

// ===== 新闻列表 =====
const loading = ref(false)
const newsList = ref<NewsItem[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 5, keyword: '', category: '' })

async function fetchNews() {
  loading.value = true
  try {
    const page = await pageNews(query)
    newsList.value = page.list
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchNews()
}

function goTo(path: string) {
  router.push(path)
}

onMounted(() => {
  loadProfile()
  fetchNews()
})
</script>

<template>
  <div class="home-page">
    <el-row :gutter="20">
      <!-- ===== 左侧：用户卡 + 快捷导航 ===== -->
      <el-col :xs="24" :sm="9" :md="8" :lg="7">
        <div class="sidebar">
          <el-card class="user-card" shadow="never">
            <div class="user-info">
              <el-avatar :size="72" :src="fileUrl(userStore.avatar)" class="user-avatar">
                {{ userStore.name.slice(0, 1) }}
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ userStore.name }}</div>
                <div class="user-role">
                  <el-tag :type="ROLE_TAG[userStore.role] || 'info'" size="small">
                    {{ ROLE_LABEL[userStore.role] || '访客' }}
                  </el-tag>
                </div>
                <div class="user-id">{{ profile?.studentId || profile?.username || userStore.name }}</div>
              </div>
            </div>
            <el-divider />
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-number">{{ profile?.department || '-' }}</span>
                <span class="stat-label">院系</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ profile?.major || '-' }}</span>
                <span class="stat-label">专业</span>
              </div>
            </div>
          </el-card>

          <!-- 快捷导航图标宫格（保留 CampusOS_a 样式） -->
          <el-card class="nav-card" shadow="never">
            <template #header>
              <span class="card-title">🚀 快捷导航</span>
            </template>
            <div class="nav-grid">
              <div
                v-for="item in visibleQuickNavItems"
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

      <!-- ===== 右侧：新闻资讯 ===== -->
      <el-col :xs="24" :sm="15" :md="16" :lg="17">
        <el-card class="main-content" shadow="never">
          <div class="section-header">
            <span class="section-title">📰 新闻资讯</span>
            <el-button text type="primary" @click="goTo('/news')">查看更多 →</el-button>
          </div>

          <div class="search-bar">
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
              <el-option v-for="c in NEWS_CATEGORIES" :key="c" :label="c" :value="c" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>&nbsp;搜索
            </el-button>
          </div>

          <div v-loading="loading" class="news-list">
            <el-empty v-if="!loading && !newsList.length" description="暂无新闻" />

            <div
              v-for="item in newsList"
              :key="item.id"
              class="news-item"
              @click="goTo(`/news/${item.id}`)"
            >
              <div class="news-info">
                <div class="news-title-row">
                  <span class="news-title">{{ item.title }}</span>
                  <span class="news-cat" :class="CATEGORY_CLASS[item.category] || 'cat-violet'">{{ item.category }}</span>
                </div>
                <p class="news-summary">{{ summary(item.content) }}</p>
                <div class="news-meta">
                  <span>✍️ {{ item.author }}</span>
                  <span>👁️ {{ item.viewCount }}</span>
                  <span>📅 {{ formatDateTime(item.publishedAt) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="query.pageNum"
              v-model:page-size="query.pageSize"
              :total="total"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next"
              background
              @size-change="handleSearch"
              @current-change="fetchNews"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 用户卡片 ===== */
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px 0;
}

.user-avatar {
  flex-shrink: 0;
  border: 3px solid #efe9fc;
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c2350;
}

.user-role {
  margin: 2px 0;
}

.user-id {
  font-size: 13px;
  color: #a89ec9;
}

.user-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 8px 6px;
  background: #f4effd;
  border-radius: 8px;
  min-width: 0;
}

.stat-number {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #2c2350;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-label {
  font-size: 12px;
  color: #a89ec9;
}

/* ===== 快捷导航宫格（保留 CampusOS_a 的彩色圆角图标） ===== */
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c2350;
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
  background: #f8f5fe;
  border: 1px solid transparent;
}

.nav-item:hover {
  background: #f4effd;
  border-color: #7c5cd6;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px #7c5cd626;
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
  color: #2c2350;
  text-align: center;
  white-space: nowrap;
}

/* ===== 右侧新闻区 ===== */
.main-content {
  min-height: 500px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c2350;
}

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
  width: 150px;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 260px;
}

.news-item {
  padding: 14px 16px;
  background: #faf8fe;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}

.news-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 26px #7c5cd61f;
}

.news-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.news-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c2350;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  margin: 6px 0;
  color: #6a628c;
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  gap: 16px;
  color: #a89ec9;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .sidebar {
    margin-bottom: 16px;
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
  .search-bar {
    flex-direction: column;
  }
  .search-select {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .nav-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
