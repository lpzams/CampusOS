<template>
  <div class="my-activities-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📋 我的活动</span>
          <span class="info-text">共 {{ list.length }} 个活动</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !list.length" description="暂未报名任何活动" />

        <div v-else class="activity-list">
          <div
            v-for="item in list"
            :key="item.id"
            class="activity-item"
            @click="goToDetail(item.id)"
          >
            <div class="item-cover">
              <el-image :src="item.coverImage" fit="cover">
                <template #placeholder>
                  <div class="image-placeholder">🎪</div>
                </template>
                <template #error>
                  <div class="image-placeholder">🎪</div>
                </template>
              </el-image>
            </div>
            <div class="item-info">
              <h4 class="item-title">
                {{ item.title }}
                <el-tag
                  :type="item.isCheckin ? 'success' : 'warning'"
                  size="small"
                >
                  {{ item.isCheckin ? '已签到' : '待签到' }}
                </el-tag>
              </h4>
              <div class="item-meta">
                <span>{{ item.category }}</span>
                <span>📍 {{ item.location }}</span>
              </div>
              <div class="item-time">
                🕐 {{ formatDateTime(item.startTime) }} ~ {{ formatDateTime(item.endTime) }}
              </div>
              <div class="item-footer">
                <span class="item-status">状态：{{ item.status }}</span>
                <span class="item-register-time">📅 报名时间：{{ formatDateTime(item.registerTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyActivities } from '@/api/activity'
import type { MyActivityItem } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref<MyActivityItem[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyActivities()
    if (res.code === 200 && res.data) {
      list.value = res.data
    }
  } catch {
    ElMessage.error('获取我的活动失败')
  } finally {
    loading.value = false
  }
}

function goToDetail(id: number) {
  router.push(`/activity/${id}`)
}

onMounted(fetchData)
</script>

<style scoped>
.my-activities-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.info-text {
  color: #909399;
  font-size: 14px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  gap: 16px;
  padding: 14px 18px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.activity-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.item-cover {
  flex-shrink: 0;
  width: 150px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
}

.item-cover .el-image {
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
  background: #f5f7fa;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.item-title {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.item-time {
  font-size: 13px;
  color: #606266;
}

.item-footer {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

@media (max-width: 640px) {
  .activity-item {
    flex-direction: column;
  }
  .item-cover {
    width: 100%;
    height: 140px;
  }
}
</style>