<template>
  <div class="activity-list-page">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-bar">
      <div class="filter-form">
        <el-select
          v-model="query.category"
          placeholder="全部分类"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="体育" value="SPORTS" />
          <el-option label="文艺" value="CULTURE" />
          <el-option label="学术" value="ACADEMIC" />
          <el-option label="志愿" value="VOLUNTEER" />
        </el-select>
        <el-select
          v-model="query.status"
          placeholder="全部状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="报名中" value="UPCOMING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <!-- 活动列表 -->
    <div v-loading="loading" class="activity-grid">
      <el-empty v-if="!loading && !list.length" description="暂无活动" />

      <div
        v-for="item in list"
        :key="item.id"
        class="activity-card"
        @click="goToDetail(item.id)"
      >
        <div class="activity-cover">
          <el-image :src="item.coverImage" fit="cover">
            <template #placeholder>
              <div class="image-placeholder">🎪</div>
            </template>
            <template #error>
              <div class="image-placeholder">🎪</div>
            </template>
          </el-image>
          <div class="activity-status" :class="getStatusClass(item.statusCode)">
            {{ item.status }}
          </div>
        </div>
        <div class="activity-info">
          <h3 class="activity-title">{{ item.title }}</h3>
          <div class="activity-meta">
            <span class="activity-category">{{ item.category }}</span>
            <span class="activity-location">📍 {{ item.location }}</span>
          </div>
          <div class="activity-time">
            🕐 {{ formatDateTime(item.startTime) }} ~ {{ formatDateTime(item.endTime) }}
          </div>
          <div class="activity-footer">
            <span class="activity-participants">
              👥 {{ item.currentParticipants }} / {{ item.maxParticipants }} 人
            </span>
            <el-tag v-if="item.isRegistered" type="success" size="small">已报名</el-tag>
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
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActivityList } from '@/api/activity'
import type { ActivityItem, ActivityCategoryCode, ActivityStatusCode } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref<ActivityItem[]>([])
const total = ref(0)

const query = reactive({
  category: '' as ActivityCategoryCode | '',
  status: '' as ActivityStatusCode | '',
  page: 1,
  size: 12,
})

function getStatusClass(statusCode: string) {
  const map: Record<string, string> = {
    UPCOMING: 'status-upcoming',
    ONGOING: 'status-ongoing',
    ENDED: 'status-ended',
  }
  return map[statusCode] || ''
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
    }
    if (query.category) params.category = query.category
    if (query.status) params.status = query.status

    const res = await getActivityList(params)
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

function handleReset() {
  query.category = ''
  query.status = ''
  query.page = 1
  fetchList()
}

function goToDetail(id: number) {
  router.push(`/activity/${id}`)
}

onMounted(fetchList)
</script>

<style scoped>
.activity-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  width: 160px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.activity-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.activity-cover {
  position: relative;
  width: 100%;
  height: 160px;
  background: #f5f7fa;
  overflow: hidden;
}

.activity-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: #f5f7fa;
}

.activity-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #fff;
}

.status-upcoming {
  background: #409eff;
}

.status-ongoing {
  background: #e6a23c;
}

.status-ended {
  background: #909399;
}

.activity-info {
  padding: 12px 14px 14px;
}

.activity-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.activity-category {
  background: #f0f2f5;
  padding: 0 8px;
  border-radius: 4px;
}

.activity-time {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 640px) {
  .filter-form {
    flex-direction: column;
  }
  .filter-select {
    width: 100% !important;
  }
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>