<script setup lang="ts">
/**
 * 课表页（功能 5）。
 *
 * 对应后端 GET /api/course/schedule。后端返回的 schedule 是「星期几 + 时间段」的行，
 * 这里先收集所有出现过的时间段做成表格行，再把课程填进对应的星期列。
 * 另外单独展示「今日课程」（GET /api/course/today）。
 */
import { computed, onMounted, ref } from 'vue'
import { getSchedule, getTodayCourses } from '@/api/course'
import type { CourseInfo, CourseSlot } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const semester = ref('2026-2027-1')
const week = ref(1)
const slots = ref<CourseSlot[]>([])
const todayCourses = ref<CourseSlot[]>([])

const WEEK_DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

/** 表格的时间段行（从返回数据里收集去重并排序） */
const timeSlots = computed(() => {
  const set = new Set<string>()
  slots.value.forEach(s => set.add(s.timeSlot))
  return [...set].sort()
})

/** 取某时间段、某星期几的课程列表（可能有多门，按周次错开） */
function coursesAt(timeSlot: string, day: number): CourseInfo[] {
  return slots.value
    .filter(s => s.timeSlot === timeSlot && Number(s.dayOfWeek) === day)
    .flatMap(s => s.courses ?? [])
}

async function fetchSchedule() {
  loading.value = true
  try {
    const result = await getSchedule({ semester: semester.value, week: week.value })
    slots.value = result.schedule ?? []
    semester.value = result.semester
    week.value = result.week
  } finally {
    loading.value = false
  }
}

async function fetchToday() {
  try {
    todayCourses.value = await getTodayCourses()
  } catch {
    // 今日课程失败不影响主课表
  }
}

onMounted(() => {
  fetchSchedule()
  fetchToday()
})
</script>

<template>
  <div>
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <span class="page-title">{{ userStore.isTeacher ? '我的教学课表' : '我的课表' }}</span>
        <div class="controls">
          <el-input v-model="semester" class="semester-input" placeholder="学期，如 2026-2027-1" />
          <span class="week-label">第</span>
          <el-input-number v-model="week" :min="1" :max="25" controls-position="right" class="week-input" />
          <span class="week-label">周</span>
          <el-button type="primary" @click="fetchSchedule">查询</el-button>
        </div>
      </div>
    </el-card>

    <!-- 今日课程 -->
    <el-card shadow="never" class="today-card">
      <template #header><span class="card-title">{{ userStore.isTeacher ? '今日授课' : '今日课程' }}</span></template>
      <div v-if="todayCourses.length === 0" class="empty-today">今天没有课，好好休息～</div>
      <div v-else class="today-list">
        <div v-for="(slot, i) in todayCourses" :key="i" class="today-item">
          <span class="today-time">{{ slot.timeSlot }}</span>
          <div class="today-courses">
            <div v-for="c in slot.courses" :key="c.id" class="today-course" :style="{ borderColor: c.color || '#7c5cd6' }">
              <strong>{{ c.name }}</strong>
              <span>{{ c.classroom }} · {{ c.teacher }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 周课表 -->
    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="!loading && timeSlots.length === 0" description="本学期暂无课表数据" />
      <table v-else class="timetable">
        <thead>
          <tr>
            <th class="time-col">时间</th>
            <th v-for="d in WEEK_DAYS" :key="d">{{ d }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ts in timeSlots" :key="ts">
            <td class="time-col">{{ ts }}</td>
            <td v-for="day in 7" :key="day" class="course-cell">
              <div
                v-for="c in coursesAt(ts, day)"
                :key="c.id"
                class="course-block"
                :style="{ background: (c.color || '#7c5cd6') + '22', borderLeftColor: c.color || '#7c5cd6' }"
              >
                <strong>{{ c.name }}</strong>
                <span>{{ c.classroom }}</span>
                <span class="course-teacher">{{ c.teacher }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </el-card>
  </div>
</template>

<style scoped>
.toolbar-card,
.today-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.semester-input {
  width: 180px;
}

.week-input {
  width: 110px;
}

.week-label {
  color: #606266;
}

.card-title {
  font-weight: 600;
}

.empty-today {
  color: #909399;
  font-size: 14px;
}

.today-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.today-item {
  display: flex;
  gap: 16px;
  align-items: center;
}

.today-time {
  color: #909399;
  font-size: 13px;
  width: 110px;
  flex-shrink: 0;
}

.today-courses {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.today-course {
  display: flex;
  flex-direction: column;
  border-left: 3px solid;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
}

.timetable {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.timetable th,
.timetable td {
  border: 1px solid #ebeef5;
  padding: 6px;
  text-align: center;
  vertical-align: top;
}

.timetable th {
  background: #f5f7fa;
  color: #606266;
  font-size: 13px;
  padding: 10px 6px;
}

.time-col {
  width: 96px;
  font-size: 12px;
  color: #909399;
}

.course-cell {
  height: 72px;
}

.course-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-left: 3px solid;
  border-radius: 4px;
  padding: 4px 6px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #303133;
  text-align: left;
}

.course-teacher {
  color: #909399;
}
</style>
