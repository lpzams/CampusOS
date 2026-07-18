<template>
  <div class="dormitory-notice-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📢 宿舍公告</span>
          <span class="info-text">共 {{ notices.length }} 条</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !notices.length" description="暂无宿舍公告" />

        <div v-else class="notice-list">
          <div
            v-for="item in notices"
            :key="item.id"
            class="notice-item"
          >
            <div class="notice-header">
              <span class="notice-title">{{ item.title }}</span>
              <!-- ✅ 使用辅助函数 -->
              <el-tag size="small" :type="getNoticeTagType(item.type)">
                {{ item.type }}
              </el-tag>
            </div>
            <p class="notice-content">{{ item.content }}</p>
            <div class="notice-time">
              📅 {{ formatDateTime(item.createTime) }}
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDormitoryNotices } from '@/api/dormitory'
import type { DormitoryNotice } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const notices = ref<DormitoryNotice[]>([])

// ===== 辅助函数：获取公告类型对应的 tag 颜色 =====
function getNoticeTagType(type: string) {
  const tagMap = {
    '维修': 'warning',
    '安全': 'danger',
    '活动': 'success',
    '通知': 'info',
  }
  return (tagMap[type as keyof typeof tagMap] || 'info') as
    | 'warning'
    | 'danger'
    | 'success'
    | 'info'
}

async function fetchNotices() {
  loading.value = true
  try {
    const res = await getDormitoryNotices()
    if (res.code === 200 && res.data) {
      notices.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchNotices)
</script>

<style scoped>
.dormitory-notice-page {
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

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notice-content {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.notice-time {
  font-size: 13px;
  color: #909399;
}
</style>