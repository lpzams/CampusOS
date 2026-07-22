<script setup lang="ts">
/**
 * 校园活动详情页（功能 13）。
 *
 * 展示活动详情 + 报名/取消报名/签到操作。
 * 报名状态靠「我的报名列表」判断：进详情时拉一次 /activity/my，
 * 找到当前活动的报名记录就说明已报名（记录 id 用于取消/签到）。
 * 对应后端 /api/activity/detail/{id}、/register、/register/{id}、/checkin、/my。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  cancelRegistration, checkinActivity, getActivityDetail, getMyActivities, registerActivity,
} from '@/api/activity'
import type { ActivityItem, ActivityRegistration } from '@/api/activity'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const acting = ref(false)
const activity = ref<ActivityItem | null>(null)
const myRegistration = ref<ActivityRegistration | null>(null)

const activityId = computed(() => Number(route.params.id))
const registered = computed(() => myRegistration.value !== null)
const canParticipate = computed(() => userStore.canAccess('/my-activities'))

async function fetchDetail() {
  loading.value = true
  try {
    activity.value = await getActivityDetail(activityId.value)
    await refreshRegistration()
  } finally {
    loading.value = false
  }
}

/** 刷新「我是否报名了这个活动」 */
async function refreshRegistration() {
  if (!userStore.isLoggedIn) return
  try {
    const mine = await getMyActivities()
    myRegistration.value = mine.find(r => Number(r.activityId) === activityId.value) ?? null
  } catch {
    myRegistration.value = null
  }
}

function ensureLogin(): boolean {
  if (userStore.isLoggedIn) return true
  ElMessage.warning('请先登录')
  router.push({ path: '/login', query: { redirect: `/activity/${activityId.value}` } })
  return false
}

async function handleRegister() {
  if (!ensureLogin()) return
  acting.value = true
  try {
    await registerActivity(activityId.value)
    ElMessage.success('报名成功')
    await fetchDetail()
  } finally {
    acting.value = false
  }
}

async function handleCancel() {
  if (!myRegistration.value) return
  acting.value = true
  try {
    await cancelRegistration(myRegistration.value.id)
    ElMessage.success('已取消报名')
    await fetchDetail()
  } finally {
    acting.value = false
  }
}

// ===== 签到弹窗 =====
const checkinVisible = ref(false)
const checkinForm = reactive({ code: '' })

async function handleCheckin() {
  acting.value = true
  try {
    await checkinActivity({ activityId: activityId.value, code: checkinForm.code })
    ElMessage.success('签到成功')
    checkinVisible.value = false
    checkinForm.code = ''
    await refreshRegistration()
  } finally {
    acting.value = false
  }
}

function progress(): number {
  const max = Number(activity.value?.maxParticipants) || 0
  const cur = Number(activity.value?.currentParticipants) || 0
  return max > 0 ? Math.min(100, Math.round((cur / max) * 100)) : 0
}

onMounted(fetchDetail)
</script>

<template>
  <div v-loading="loading">
    <el-page-header content="活动详情" class="page-header" @back="router.back()" />

    <el-card v-if="activity" shadow="never">
      <div class="detail-head">
        <h1 class="detail-title">{{ activity.title }}</h1>
        <el-tag>{{ activity.category || activity.categoryCode }}</el-tag>
      </div>

      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="开始时间">{{ formatDateTime(activity.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(activity.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="活动地点">{{ activity.location }}</el-descriptions-item>
        <el-descriptions-item label="活动状态">{{ activity.status }}</el-descriptions-item>
      </el-descriptions>

      <div class="progress-block">
        <div class="progress-text">
          <span>报名人数 {{ activity.currentParticipants ?? 0 }} / {{ activity.maxParticipants ?? 0 }}</span>
          <span v-if="registered" class="registered-tag">已报名</span>
        </div>
        <el-progress :percentage="progress()" color="#7c5cd6" />
      </div>

      <el-divider v-if="activity.content" content-position="left">活动介绍</el-divider>
      <div v-if="activity.content" class="detail-content">{{ activity.content }}</div>

      <div v-if="canParticipate" class="detail-actions">
        <el-button v-if="!registered" type="primary" :loading="acting" @click="handleRegister">立即报名</el-button>
        <template v-else>
          <el-button type="success" :disabled="myRegistration?.checkedIn" @click="checkinVisible = true">
            {{ myRegistration?.checkedIn ? '已签到' : '活动签到' }}
          </el-button>
          <el-button :loading="acting" @click="handleCancel">取消报名</el-button>
        </template>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="活动不存在" />

    <!-- 签到弹窗 -->
    <el-dialog v-model="checkinVisible" title="活动签到" width="360px">
      <el-input v-model="checkinForm.code" placeholder="请输入现场签到码" />
      <template #footer>
        <el-button @click="checkinVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="handleCheckin">签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.detail-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.detail-info {
  margin-bottom: 20px;
}

.progress-block {
  margin-bottom: 8px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #606266;
  margin-bottom: 6px;
}

.registered-tag {
  color: #67c23a;
  font-weight: 600;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
</style>
