<template>
  <div class="exam-calendar-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📅 考试日历</span>
          <div class="toolbar-right">
            <el-button size="small" @click="changeMonth(-1)">◀ 上月</el-button>
            <span class="month-label">{{ currentMonth }}</span>
            <el-button size="small" @click="changeMonth(1)">下月 ▶</el-button>
            <el-button size="small" type="primary" @click="fetchData">今天</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !hasEvents" description="本月暂无考试安排" />

        <div v-else class="calendar-grid">
          <!-- 星期头 -->
          <div class="week-header">
            <div v-for="day in weekDays" :key="day" class="week-day">
              {{ day }}
            </div>
          </div>

          <!-- 日历格子 -->
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            class="calendar-day"
            :class="{
              'empty-day': !day,
              'has-exam': day && day.exams && day.exams.length > 0,
            }"
          >
            <template v-if="day">
              <div class="day-number">{{ day.date }}</div>
              <div class="day-exams">
                <div
                  v-for="exam in day.exams"
                  :key="exam.id"
                  class="day-exam"
                  :class="{
                    'exam-pending': exam.status === 'PENDING',
                    'exam-completed': exam.status === 'COMPLETED',
                  }"
                  @click="goToDetail(exam.id)"
                  :title="`${exam.courseName} ${exam.time}`"
                >
                  <span class="exam-name">{{ exam.courseName }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExamCalendar } from '@/api/exam'
import type { ExamDayEvent } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const events = ref<ExamDayEvent[]>([])

const current = reactive({
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
})

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const currentMonth = computed(() => {
  return `${current.year}年${current.month}月`
})

const hasEvents = computed(() => events.value.length > 0)

// ===== 生成日历数据 =====
const calendarDays = computed(() => {
  const days = getDaysInMonth(current.year, current.month)
  const firstDayOfWeek = new Date(current.year, current.month - 1, 1).getDay()

  // 补齐前面的空位
  const result: ({ date: number; exams: ExamDayEvent['exams'] } | null)[] = []
  for (let i = 0; i < firstDayOfWeek; i++) {
    result.push(null)
  }

  // 填充日期
  for (let d = 1; d <= days; d++) {
    const dateStr = `${current.year}-${String(current.month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const dayEvent = events.value.find(e => e.date === dateStr)
    result.push({
      date: d,
      exams: dayEvent?.exams || [],
    })
  }

  return result
})

function getDaysInMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}

// ===== 获取数据 =====
async function fetchData() {
  const monthStr = `${current.year}-${String(current.month).padStart(2, '0')}`
  loading.value = true
  try {
    const res = await getExamCalendar({ month: monthStr })
    if (res.code === 200 && res.data) {
      events.value = res.data.events || []
    }
  } finally {
    loading.value = false
  }
}

// ===== 切换月份 =====
function changeMonth(delta: number) {
  current.month += delta
  if (current.month > 12) {
    current.month = 1
    current.year++
  } else if (current.month < 1) {
    current.month = 12
    current.year--
  }
  fetchData()
}

// ===== 跳转考试详情（列表页） =====
function goToDetail(examId: number) {
  router.push('/exam')
}

onMounted(fetchData)
</script>

<style scoped>
.exam-calendar-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.month-label {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  min-width: 100px;
  text-align: center;
}

/* ===== 日历网格 ===== */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  min-height: 400px;
}

.week-header {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.week-day {
  padding: 8px 4px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
  border-radius: 4px;
}

.calendar-day {
  min-height: 80px;
  padding: 6px 4px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  transition: border-color 0.2s;
}

.calendar-day:hover {
  border-color: #409eff;
}

.empty-day {
  background: transparent;
  border: 1px solid transparent;
}

.has-exam {
  background: #ecf5ff;
  border-color: #409eff;
}

.day-number {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.day-exams {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.day-exam {
  padding: 2px 6px;
  font-size: 11px;
  border-radius: 3px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: transform 0.15s;
}

.day-exam:hover {
  transform: scale(1.02);
}

.exam-pending {
  background: #f56c6c;
  color: #fff;
}

.exam-completed {
  background: #67c23a;
  color: #fff;
}

@media (max-width: 640px) {
  .calendar-day {
    min-height: 60px;
    padding: 4px 2px;
  }
  .day-number {
    font-size: 12px;
  }
  .day-exam {
    font-size: 10px;
    padding: 1px 4px;
  }
}
</style>