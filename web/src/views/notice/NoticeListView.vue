<script setup lang="ts">
/**
 * 通知公告列表页（功能 4）。
 *
 * 对应后端 GET /api/notice/list（分页参数 page/size）。
 * 顶部显示未读数（登录后才有），可按类型筛选。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NOTICE_TYPES, getUnreadCount, pageNotices } from '@/api/notice'
import type { NoticeItem } from '@/api/notice'
import { formatDateTime, summary } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const list = ref<NoticeItem[]>([])
const total = ref(0)
const unread = ref(0)

const query = reactive({ type: '', department: '', page: 1, size: 10 })

async function fetchList() {
  loading.value = true
  try {
    const page = await pageNotices(query)
    list.value = page.list
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    unread.value = (await getUnreadCount()).count
  } catch {
    // 未读数拿不到不影响列表展示，忽略
  }
}

function handleFilter() {
  query.page = 1
  fetchList()
}

function goDetail(id: number) {
  router.push(`/notice/${id}`)
}

onMounted(() => {
  fetchList()
  fetchUnread()
})
</script>

<template>
  <div>
    <!-- hero：与资讯页同一套梦幻语言，用玫瑰色区分模块 -->
    <section class="notice-hero">
      <span class="hero-eyebrow">NOTICE CENTER · 通知公告</span>
      <h1>重要的事情，<em>一条不落</em></h1>
      <p>
        学校与院系的官方通知
        <template v-if="userStore.isLoggedIn && unread > 0"> · 你还有 {{ unread }} 条未读</template>
      </p>
    </section>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <span class="page-title">全部公告</span>
        <el-select v-model="query.type" placeholder="全部类型" clearable class="type-select" @change="handleFilter">
          <el-option v-for="t in NOTICE_TYPES" :key="t.code" :label="t.name" :value="t.code" />
        </el-select>
      </div>
    </el-card>

    <div v-loading="loading" class="notice-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无公告" />
      <el-card
        v-for="item in list"
        :key="item.id"
        shadow="never"
        class="notice-card"
        :class="item.type === 'SCHOOL' ? 'is-school' : 'is-dept'"
        @click="goDetail(item.id)"
      >
        <div class="notice-head">
          <span class="notice-title">{{ item.title }}</span>
          <span class="notice-chip" :class="item.type === 'SCHOOL' ? 'rose' : 'teal'">
            {{ item.type === 'SCHOOL' ? '学校' : '院系' }}
          </span>
        </div>
        <p class="notice-summary">{{ summary(item.content) }}</p>
        <div class="notice-meta">
          <span>{{ item.department || '学校办公室' }}</span>
          <span>{{ formatDateTime(item.createTime) }}</span>
        </div>
      </el-card>
    </div>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next, sizes"
        background
        @current-change="fetchList"
        @size-change="handleFilter"
      />
    </div>
  </div>
</template>

<style scoped>
.notice-hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
  padding: 34px 36px 30px;
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(120deg, #7a2f57 0%, #b8496d 55%, #e08ba0 100%);
  box-shadow: 0 16px 40px #b8496d33;
}

.notice-hero::after {
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

.notice-hero h1 {
  margin: 10px 0 8px;
  font: 700 30px/1.35 Georgia, "STSong", serif;
}

.notice-hero h1 em {
  color: #ffd98a;
  font-style: normal;
}

.notice-hero p {
  margin: 0;
  color: #ffffffb3;
  font-size: 13px;
}

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2350;
}

.type-select {
  width: 160px;
}

.notice-card {
  margin-bottom: 12px;
  cursor: pointer;
  border-left: 4px solid #e2d8f7;
  transition: transform .18s, box-shadow .18s;
}

.notice-card.is-school { border-left-color: #e08ba0; }
.notice-card.is-dept { border-left-color: #7fd0c6; }

.notice-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 34px #b8496d1f;
}

.notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2350;
}

.notice-chip {
  flex-shrink: 0;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.notice-chip.rose { color: #b8496d; background: #fde9f0; }
.notice-chip.teal { color: #1d7a72; background: #e2f6f2; }

.notice-summary {
  margin: 8px 0;
  color: #6a628c;
  font-size: 14px;
  line-height: 1.6;
}

.notice-meta {
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
  .notice-hero { padding: 24px 20px; }
  .notice-hero h1 { font-size: 22px; }
}
</style>
