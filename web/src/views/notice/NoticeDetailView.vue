<template>
  <div v-loading="loading" class="notice-detail-page">
    <div v-if="detail" class="detail-card">
      <!-- 返回 -->
      <el-page-header content="公告详情" class="page-header" @back="router.back()" />

      <!-- 标题 -->
      <div class="detail-header">
        <h1 class="detail-title">
          {{ detail.title }}
          <el-tag v-if="detail.isTop" type="danger" size="small">置顶</el-tag>
        </h1>
        <div class="detail-meta">
          <el-tag :type="detail.type === 'SCHOOL' ? 'danger' : 'primary'" size="small">
            {{ detail.typeDesc }}
          </el-tag>
          <span>🏢 {{ detail.department }}</span>
          <span>📅 发布：{{ formatDateTime(detail.createTime) }}</span>
          <span>⏰ 截止：{{ formatDateTime(detail.deadline) }}</span>
          <span>👁️ {{ detail.readCount }}</span>
        </div>
      </div>

      <el-divider />

      <!-- 内容 -->
      <div class="detail-content" v-html="detail.content" />

      <!-- 附件 -->
      <div v-if="detail.attachments && detail.attachments.length > 0" class="attachments">
        <el-divider content-position="left">附件</el-divider>
        <div
          v-for="file in detail.attachments"
          :key="file.id"
          class="attachment-item"
          @click="downloadAttachment(file.url, file.name)"
        >
          <el-icon><Document /></el-icon>
          <span>{{ file.name }}</span>
          <span class="file-size">{{ formatFileSize(file.size) }}</span>
        </div>
      </div>

      <!-- 底部 -->
      <div class="detail-footer">
        <el-button
          v-if="!detail.isRead"
          type="primary"
          @click="handleMarkRead"
        >
          标记为已读
        </el-button>
        <el-button v-else type="success" plain disabled>
          ✅ 已读
        </el-button>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="公告不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getNoticeDetail, markNoticeRead } from '@/api/notice'
import type { NoticeDetail } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<NoticeDetail | null>(null)

async function fetchDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getNoticeDetail(String(id))
    if (res.code === 200 && res.data) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
}

async function handleMarkRead() {
  if (!detail.value) return
  try {
    const res = await markNoticeRead(detail.value.id)
    if (res.code === 200) {
      detail.value.isRead = true
      ElMessage.success('已标记为已读')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

function downloadAttachment(url: string, name: string) {
  window.open(url, '_blank')
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}

onMounted(fetchDetail)
</script>

<style scoped>
.notice-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.detail-card {
  background: #fff;
  padding: 30px 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.page-header {
  margin-bottom: 16px;
  padding: 0;
}

.detail-header {
  margin-bottom: 8px;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  color: #909399;
  font-size: 14px;
}

.detail-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  min-height: 100px;
}

.detail-content :deep(p) {
  margin: 0 0 12px 0;
}
.detail-content :deep(img) {
  max-width: 100%;
}

.attachments {
  margin-top: 8px;
}

.attachment-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  margin: 4px 8px 4px 0;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
}

.attachment-item:hover {
  background: #e6f0ff;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.detail-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

@media (max-width: 640px) {
  .detail-card {
    padding: 16px;
  }
  .detail-title {
    font-size: 18px;
  }
}
</style>