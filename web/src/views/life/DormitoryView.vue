<script setup lang="ts">
/**
 * 宿舍管理页（功能 10）。
 *
 * 左边宿舍信息（房间/成员/设施/水电余额），右边宿舍公告。
 * 对应后端 /api/dormitory/info、/utility、/notice。
 */
import { onMounted, ref } from 'vue'
import { getDormitoryInfo, getDormitoryNotices } from '@/api/dormitory'
import type { DormNotice, DormitoryInfo } from '@/api/dormitory'
import { formatDateTime } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const loading = ref(false)
const info = ref<DormitoryInfo | null>(null)
const notices = ref<DormNotice[]>([])

async function fetchAll() {
  loading.value = true
  try {
    info.value = await getDormitoryInfo()
  } catch {
    // 没分配宿舍时后端报 404，页面给空态即可
  }
  try {
    notices.value = await getDormitoryNotices()
  } catch {
    // 公告失败忽略
  } finally {
    loading.value = false
  }
}

onMounted(fetchAll)
</script>

<template>
  <div v-loading="loading" class="dorm-layout">
    <ServiceHero
      class="dorm-hero"
      eyebrow="CAMPUS LIVING"
      title="宿舍管理"
      description="查看房间成员、设施与水电余额，宿舍公告和生活缴费集中在这里。"
      icon="⌂"
      tone="green"
      :metrics="[
        { label: '房间', value: info ? `${info.building} ${info.room}` : '未分配' },
        { label: '电费余额', value: info?.electricityBalance ?? '-' },
        { label: '宿舍公告', value: notices.length },
      ]"
    >
      <template #actions><el-button type="primary" plain @click="fetchAll">刷新信息</el-button></template>
    </ServiceHero>
    <!-- 宿舍信息 -->
    <div class="left-col">
      <el-card v-if="info" shadow="never">
        <template #header>
          <div class="dorm-header">
            <span class="dorm-title">{{ info.building }} {{ info.room }}</span>
            <el-tag size="small">{{ info.type }}</el-tag>
          </div>
        </template>

        <div class="utility-row">
          <div class="utility-item">
            <span class="utility-label">电费余额</span>
            <span class="utility-value">{{ info.electricityBalance ?? '-' }} 度</span>
          </div>
          <div class="utility-item">
            <span class="utility-label">水费余额</span>
            <span class="utility-value">{{ info.waterBalance ?? '-' }} 吨</span>
          </div>
        </div>

        <el-divider content-position="left">宿舍成员</el-divider>
        <div class="members">
          <div v-for="m in info.members" :key="m.userId" class="member">
            <el-avatar :size="36">{{ (m.realName || '?').slice(0, 1) }}</el-avatar>
            <div class="member-info">
              <span class="member-name">
                {{ m.realName }}
                <el-tag v-if="m.isRoomLeader" size="small" type="success">寝室长</el-tag>
              </span>
              <span class="member-phone">{{ m.phone || '-' }}</span>
            </div>
          </div>
        </div>

        <el-divider content-position="left">宿舍设施</el-divider>
        <div class="facilities">
          <el-tag v-for="f in info.facilities" :key="f" class="facility-tag" effect="plain">{{ f }}</el-tag>
        </div>
      </el-card>

      <el-empty v-else-if="!loading" description="暂未分配宿舍信息" />
    </div>

    <!-- 宿舍公告 -->
    <div class="right-col">
      <el-card shadow="never">
        <template #header><span class="card-title">宿舍公告</span></template>
        <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="80" />
        <div v-for="n in notices" :key="n.id" class="notice-item">
          <div class="notice-title">{{ n.title }}</div>
          <div class="notice-content">{{ n.content }}</div>
          <div class="notice-time">{{ formatDateTime(n.createTime) }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dorm-layout {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 16px;
  align-items: start;
}

.dorm-hero {
  grid-column: 1 / -1;
}

.dorm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dorm-title {
  font-size: 18px;
  font-weight: 600;
}

.utility-row {
  display: flex;
  gap: 16px;
}

.utility-item {
  flex: 1;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.utility-label {
  color: #909399;
  font-size: 13px;
}

.utility-value {
  font-size: 22px;
  font-weight: 700;
  color: #7c5cd6;
}

.members {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-info {
  display: flex;
  flex-direction: column;
}

.member-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.member-phone {
  color: #909399;
  font-size: 13px;
}

.facilities {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.card-title {
  font-weight: 600;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.notice-content {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.notice-time {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .dorm-layout {
    grid-template-columns: 1fr;
  }
}
</style>
