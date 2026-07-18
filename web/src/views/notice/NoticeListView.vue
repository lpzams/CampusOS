<template>
  <div class="notice-list-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-bar">
      <div class="filter-form">
        <el-select
          v-model="query.type"
          placeholder="全部类型"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="学校公告" value="SCHOOL" />
          <el-option label="院系公告" value="DEPT" />
        </el-select>
        <el-input
          v-model="query.department"
          placeholder="院系名称"
          clearable
          class="filter-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 公告列表 -->
    <div v-loading="loading" class="notice-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无公告" />

      <div
        v-for="item in list"
        :key="item.id"
        class="notice-item"
        :class="{ 'is-read': item.isRead }"
        @click="goToDetail(item.id)"
      >
        <div class="notice-left">
          <span v-if="item.isTop" class="top-badge">置顶</span>
          <span v-if="!item.isRead" class="unread-dot">●</span>
          <span class="notice-title">{{ item.title }}</span>
        </div>
        <div class="notice-right">
          <el-tag :type="item.type === 'SCHOOL' ? 'danger' : 'primary'" size="small">
            {{ item.typeDesc }}
          </el-tag>
          <span class="notice-dept">{{ item.department }}</span>
          <span class="notice-time">{{ formatDateTime(item.createTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNoticeList } from '@/api/notice'
import type { NoticeItem } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref<NoticeItem[]>([])
const total = ref(0)

const query = reactive({
  type: '' as '' | 'SCHOOL' | 'DEPT',
  department: '',
  page: 1,
  size: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getNoticeList({
      type: query.type || undefined,
      department: query.department || undefined,
      page: query.page,
      size: query.size,
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

function goToDetail(id: number) {
  router.push(`/notice/${id}`)
}

onMounted(fetchList)
</script>

<style scoped>
.notice-list-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.filter-bar {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  width: 160px;
}

.filter-input {
  width: 220px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-height: 300px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}

.notice-item:hover {
  background: #f5f7fa;
}

.notice-item.is-read .notice-title {
  color: #909399;
}

.notice-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.top-badge {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 600;
  flex-shrink: 0;
}

.unread-dot {
  color: #409eff;
  font-size: 10px;
  flex-shrink: 0;
}

.notice-title {
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-left: 16px;
}

.notice-dept {
  color: #606266;
  font-size: 13px;
}

.notice-time {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

@media (max-width: 640px) {
  .notice-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  .notice-right {
    margin-left: 0;
    flex-wrap: wrap;
  }
  .filter-form {
    flex-direction: column;
  }
  .filter-select,
  .filter-input {
    width: 100% !important;
  }
}
</style>