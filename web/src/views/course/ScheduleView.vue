<template>
  <div class="schedule-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📚 个人课表</span>
          <div class="toolbar-right">
            <span class="info-text">{{ schedule?.semester }} | 第 {{ schedule?.week || 1 }} 周</span>
            <el-button size="small" @click="fetchSchedule">刷新</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="schedule-container">
        <el-empty v-if="!loading && !hasData" description="暂无课程安排" />

        <!-- 课表表格 -->
        <table v-else class="schedule-table">
          <thead>
            <tr>
              <th class="time-header">时间</th>
              <th v-for="day in weekDays" :key="day.key" class="day-header">
                {{ day.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(slot, index) in timeSlots" :key="index">
              <td class="time-cell">{{ slot }}</td>
              <td
                v-for="day in weekDays"
                :key="day.key"
                class="course-cell"
              >
                <template v-if="getCourse(day.key, slot)">
                  <div
                    class="course-block"
                    :style="{ backgroundColor: getCourse(day.key, slot)?.color || '#409EFF' }"
                  >
                    <div class="course-name">{{ getCourse(day.key, slot)?.name }}</div>
                    <div class="course-info">{{ getCourse(day.key, slot)?.classroom }}</div>
                    <div class="course-info">{{ getCourse(day.key, slot)?.teacher }}</div>
                  </div>
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getSchedule } from '@/api/course'
import type { ScheduleResponse, ScheduleSlot } from '@/api/types'

const loading = ref(false)
const schedule = ref<ScheduleResponse | null>(null)

// ===== 课表配置 =====
const weekDays = [
  { key: 1, label: '周一' },
  { key: 2, label: '周二' },
  { key: 3, label: '周三' },
  { key: 4, label: '周四' },
  { key: 5, label: '周五' },
  { key: 6, label: '周六' },
  { key: 7, label: '周日' },
]

const timeSlots = [
  '08:00-09:35',
  '10:00-11:35',
  '14:00-15:35',
  '16:00-17:35',
  '19:00-20:35',
]

const hasData = computed(() => {
  return schedule.value?.schedule?.some((slot: ScheduleSlot) => slot.courses.length > 0) || false
})

// ===== 获取某天某时段的课程 =====
function getCourse(dayOfWeek: number, timeSlot: string) {
  const day = schedule.value?.schedule?.find((s: ScheduleSlot) => s.dayOfWeek === dayOfWeek)
  if (!day) return null
  // 一个时段可能有多门课，取第一门（简化处理）
  return day.courses.find((c: any) => c.timeSlot === timeSlot) || day.courses[0] || null
}

// ===== 获取数据 =====
async function fetchSchedule() {
  loading.value = true
  try {
    const res = await getSchedule()
    if (res.code === 200 && res.data) {
      schedule.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchSchedule)
</script>

<style scoped>
.schedule-page {
  max-width: 1200px;
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
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.info-text {
  color: #909399;
  font-size: 14px;
}
.schedule-container {
  overflow-x: auto;
}
.schedule-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}
.schedule-table th,
.schedule-table td {
  border: 1px solid #ebeef5;
  padding: 4px;
  text-align: center;
}
.time-header {
  background: #f5f7fa;
  width: 100px;
  font-weight: 600;
  font-size: 13px;
}
.day-header {
  background: #f5f7fa;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 4px !important;
}
.time-cell {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.course-cell {
  height: 70px;
  vertical-align: middle;
}
.course-block {
  padding: 4px 6px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  min-height: 55px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.15s;
}
.course-block:hover {
  transform: scale(1.02);
}
.course-name {
  font-weight: 600;
  font-size: 13px;
}
.course-info {
  font-size: 11px;
  opacity: 0.9;
}
</style>