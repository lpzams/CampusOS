<script setup lang="ts">
/**
 * 我的活动(整合自 CampusOS_a 的 MyActivitiesView)。
 *
 * 对应后端 GET /api/activity/my(返回报名记录列表),
 * 再按 activityId 拉活动详情拼出完整卡片;支持取消报名,点击卡片进活动详情。
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelRegistration, getActivityDetail, getMyActivities } from '@/api/activity'
import type { ActivityItem, ActivityRegistration } from '@/api/activity'
import { formatDateTime } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()

interface MyActivityRow {
  registration: ActivityRegistration
  activity: ActivityItem | null
}

const loading = ref(false)
const list = ref<MyActivityRow[]>([])

async function fetchData() {
  loading.value = true
  try {
    const registrations = await getMyActivities()
    // 逐条补活动详情;个别活动可能已被删除,详情失败不影响其它行
    list.value = await Promise.all(registrations.map(async registration => ({
      registration,
      activity: await getActivityDetail(registration.activityId).catch(() => null),
    })))
  } finally {
    loading.value = false
  }
}

async function handleCancel(row: MyActivityRow, event: MouseEvent) {
  event.stopPropagation()
  const title = row.activity?.title || '该活动'
  const confirmed = await ElMessageBox.confirm(`确定取消报名「${title}」吗?`, '取消报名', {
    confirmButtonText: '确定',
    cancelButtonText: '再想想',
    type: 'warning',
  }).catch(() => false)
  if (!confirmed) return
  await cancelRegistration(row.registration.id)
  list.value = list.value.filter(item => item.registration.id !== row.registration.id)
  ElMessage.success('已取消报名')
}

function goDetail(row: MyActivityRow) {
  router.push(`/activity/${row.registration.activityId}`)
}

onMounted(fetchData)
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="MY ACTIVITIES"
      title="我的活动"
      description="报过名的活动都在这里,记得按时签到;没空参加就提前取消,把名额留给别人。"
      icon="🎪"
      tone="violet"
      :metrics="[{ label: '已报名', value: list.length, hint: '个活动' }]"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/activity')">去逛活动广场</el-button>
      </template>
    </ServiceHero>

    <div v-loading="loading" class="activity-list">
      <el-empty v-if="!loading && !list.length" description="暂未报名任何活动,去活动广场看看吧" />

      <el-card
        v-for="row in list"
        :key="row.registration.id"
        shadow="never"
        class="activity-card"
        @click="goDetail(row)"
      >
        <div class="row-body">
          <div class="item-info">
            <div class="item-title">
              {{ row.activity?.title || `活动 #${row.registration.activityId}` }}
              <el-tag :type="row.registration.checkedIn ? 'success' : 'warning'" size="small">
                {{ row.registration.checkedIn ? '已签到' : '待签到' }}
              </el-tag>
              <el-tag v-if="row.activity?.status" size="small" type="info">{{ row.activity.status }}</el-tag>
            </div>
            <div class="item-meta">
              <span v-if="row.activity?.category">🏷️ {{ row.activity.category }}</span>
              <span v-if="row.activity?.location">📍 {{ row.activity.location }}</span>
            </div>
            <div v-if="row.activity?.startTime" class="item-time">
              🕐 {{ formatDateTime(row.activity.startTime) }} ~ {{ formatDateTime(row.activity.endTime) }}
            </div>
            <div class="item-footer">
              📅 报名于 {{ formatDateTime(row.registration.createTime) }}
            </div>
          </div>
          <div class="item-actions">
            <el-button
              v-if="!row.registration.checkedIn"
              size="small"
              type="danger"
              plain
              @click="handleCancel(row, $event)"
            >
              取消报名
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.activity-card {
  cursor: pointer;
  transition: transform .18s, box-shadow .18s;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 34px #7c5cd626;
}

.row-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2350;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #a89ec9;
}

.item-time {
  font-size: 13px;
  color: #6a628c;
}

.item-footer {
  font-size: 12px;
  color: #a89ec9;
}

.item-actions {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .row-body {
    flex-direction: column;
    align-items: stretch;
  }
  .item-actions {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
