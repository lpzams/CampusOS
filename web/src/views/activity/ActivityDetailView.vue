<template>
  <div v-loading="loading" class="activity-detail-page">
    <div v-if="detail" class="detail-container">
      <!-- 返回 -->
      <el-page-header content="活动详情" @back="goBack" />

      <!-- 封面 -->
      <div v-if="detail.coverImage" class="cover-wrapper">
        <el-image :src="detail.coverImage" fit="cover" style="width: 100%; max-height: 350px;" />
      </div>

      <!-- 标题 -->
      <h1 class="detail-title">{{ detail.title }}</h1>

      <!-- 信息栏 -->
      <div class="detail-meta">
        <el-tag :type="getCategoryTagType(detail.categoryCode)" size="default">
          {{ detail.category }}
        </el-tag>
        <el-tag :type="getStatusTagType(detail.statusCode)" size="default">
          {{ detail.status }}
        </el-tag>
        <span>📍 {{ detail.location }}</span>
        <span>👤 {{ detail.organizer }}</span>
        <span>📞 {{ detail.contactPhone }}</span>
      </div>

      <el-divider />

      <!-- 时间与人数 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(detail.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ formatDateTime(detail.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="报名人数">
          {{ detail.currentParticipants }} / {{ detail.maxParticipants }} 人
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detail.statusCode)">
            {{ detail.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">活动详情</el-divider>
      <div class="detail-content" v-html="detail.content" />

      <!-- 操作按钮 -->
      <div class="action-bar">
        <!-- 未报名 + 活动可报名（UPCOMING 或 ONGOING） -->
        <template v-if="!detail.isRegistered && ['UPCOMING', 'ONGOING'].includes(detail.statusCode)">
          <el-button
            type="primary"
            size="large"
            :loading="actionLoading"
            @click="handleRegister"
          >
            立即报名
          </el-button>
        </template>

        <!-- 已报名 + 活动可签到（UPCOMING 或 ONGOING） -->
        <template v-if="detail.isRegistered && ['UPCOMING', 'ONGOING'].includes(detail.statusCode)">
          <el-button
            type="success"
            plain
            size="large"
            @click="openCheckinDialog"
          >
            📍 签到
          </el-button>
          <el-button
            type="danger"
            plain
            size="large"
            :loading="actionLoading"
            @click="handleCancelRegister"
          >
            取消报名
          </el-button>
        </template>

        <!-- 已结束的活动 -->
        <template v-if="detail.statusCode === 'ENDED'">
          <el-tag type="info" size="large">活动已结束</el-tag>
        </template>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="活动不存在" />

    <!-- ===== 签到弹窗 ===== -->
    <el-dialog v-model="checkinDialogVisible" title="活动签到" width="400px">
      <div class="checkin-form">
        <p class="checkin-tip">
          请输入签到码完成签到，签到码在报名成功时获得
        </p>
        <el-input
          v-model="checkinCode"
          placeholder="请输入签到码"
          size="large"
          @keyup.enter="confirmCheckin"
        />
      </div>
      <template #footer>
        <el-button @click="checkinDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkinLoading" @click="confirmCheckin">
          确认签到
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityDetail, registerActivity, cancelActivityRegister, checkinActivity } from '@/api/activity'
import { useUserStore } from '@/stores/user'
import type { ActivityDetail } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const actionLoading = ref(false)
const checkinLoading = ref(false)
const detail = ref<ActivityDetail | null>(null)

const checkinDialogVisible = ref(false)
const checkinCode = ref('')

// ===== ✅ 辅助函数：分类标签颜色 =====
function getCategoryTagType(categoryCode: string) {
  const map: Record<string, 'danger' | 'warning' | 'primary' | 'success' | 'info'> = {
    SPORTS: 'danger',
    CULTURE: 'warning',
    ACADEMIC: 'primary',
    VOLUNTEER: 'success',
  }
  return map[categoryCode] || 'info'
}

// ===== ✅ 辅助函数：状态标签颜色 =====
function getStatusTagType(statusCode: string) {
  const map: Record<string, 'warning' | 'success' | 'info'> = {
    UPCOMING: 'warning',
    ONGOING: 'success',
    ENDED: 'info',
  }
  return map[statusCode] || 'info'
}

// ===== 获取详情 =====
async function fetchDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getActivityDetail(String(id))
    if (res.code === 200 && res.data) {
      detail.value = res.data
    } else {
      ElMessage.warning(res.msg || '获取详情失败')
    }
  } finally {
    loading.value = false
  }
}

// ===== 报名 =====
async function handleRegister() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!detail.value) return

  try {
    await ElMessageBox.confirm(`确定报名「${detail.value.title}」吗？`, '报名确认', {
      confirmButtonText: '确定报名',
      type: 'info',
    })
  } catch {
    return
  }

  actionLoading.value = true
  try {
    const res = await registerActivity({
      activityId: detail.value.id,
      remark: '',
    })
    if (res.code === 200 && res.data) {
      ElMessage.success(`报名成功！签到码：${res.data.checkinCode}`)
      await fetchDetail()
    } else {
      ElMessage.error(res.msg || '报名失败')
    }
  } catch {
    ElMessage.error('报名失败，请重试')
  } finally {
    actionLoading.value = false
  }
}

// ===== 取消报名 =====
async function handleCancelRegister() {
  if (!detail.value) return

  try {
    await ElMessageBox.confirm(`确定取消报名「${detail.value.title}」吗？`, '取消报名', {
      confirmButtonText: '确定取消',
      type: 'warning',
    })
  } catch {
    return
  }

  actionLoading.value = true
  try {
    const res = await cancelActivityRegister(detail.value.id)
    if (res.code === 200) {
      ElMessage.success('取消报名成功')
      await fetchDetail()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch {
    ElMessage.error('取消失败，请重试')
  } finally {
    actionLoading.value = false
  }
}

// ===== 签到弹窗 =====
function openCheckinDialog() {
  if (!detail.value) return
  checkinCode.value = ''
  checkinDialogVisible.value = true
}

async function confirmCheckin() {
  if (!checkinCode.value.trim()) {
    ElMessage.warning('请输入签到码')
    return
  }
  if (!detail.value) return

  checkinLoading.value = true
  try {
    const res = await checkinActivity({
      activityId: detail.value.id,
      code: checkinCode.value.trim(),
    })
    if (res.code === 200 && res.data) {
      ElMessage.success(`签到成功！签到时间：${formatDateTime(res.data.checkinTime)}`)
      checkinDialogVisible.value = false
      await fetchDetail()
    } else {
      ElMessage.error(res.msg || '签到失败')
    }
  } catch {
    ElMessage.error('签到失败，请重试')
  } finally {
    checkinLoading.value = false
  }
}

function goBack() {
  router.push('/activities')
}

onMounted(fetchDetail)
</script>

<style scoped>
.activity-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.detail-container {
  background: #fff;
  padding: 24px 32px 32px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.cover-wrapper {
  margin: 16px 0 20px;
  border-radius: 8px;
  overflow: hidden;
}

.detail-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: #303133;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
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

.action-bar {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.checkin-form {
  padding: 8px 0;
}

.checkin-tip {
  color: #909399;
  font-size: 14px;
  margin: 0 0 16px 0;
}

@media (max-width: 640px) {
  .detail-container {
    padding: 16px;
  }
  .detail-title {
    font-size: 20px;
  }
}
</style>