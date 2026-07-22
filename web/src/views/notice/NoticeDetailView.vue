<script setup lang="ts">
/**
 * 通知公告详情页（功能 4）。
 *
 * 对应后端 GET /api/notice/detail/{id}。
 * 登录用户打开详情时自动标记已读（POST /api/notice/read/{id}），
 * 用来维护列表页顶部的未读数。
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoticeDetail, markNoticeRead } from '@/api/notice'
import type { NoticeItem } from '@/api/notice'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const notice = ref<NoticeItem | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    const id = route.params.id as string
    notice.value = await getNoticeDetail(id)
    // 登录用户才标记已读；失败不影响阅读，静默忽略
    if (userStore.isLoggedIn) markNoticeRead(id).catch(() => {})
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <el-page-header content="公告详情" class="page-header" @back="router.back()" />

    <el-card v-if="notice" shadow="never">
      <h1 class="detail-title">{{ notice.title }}</h1>
      <div class="detail-meta">
        <el-tag v-if="notice.type" size="small" :type="notice.type === 'SCHOOL' ? 'danger' : 'info'">
          {{ notice.type === 'SCHOOL' ? '学校通知' : '院系通知' }}
        </el-tag>
        <span>{{ notice.department || '学校办公室' }}</span>
        <span>{{ formatDateTime(notice.createTime) }}</span>
      </div>
      <el-divider />
      <div class="detail-content">{{ notice.content }}</div>
    </el-card>

    <el-empty v-else-if="!loading" description="公告不存在" />
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.detail-title {
  margin: 0 0 12px;
  font-size: 22px;
  color: #303133;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #909399;
  font-size: 13px;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}
</style>
