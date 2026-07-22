<script setup lang="ts">
/**
 * 考试日历(整合自 CampusOS_a 的 ExamCalendarView)。
 *
 * 对应后端 GET /api/exam/calendar?month=YYYY-MM,返回当月考试扁平列表,
 * 前端按 examDate 归组渲染成月历格子。点击考试跳到考试安排列表页。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getExamCalendar } from '@/api/exam'
import type { ExamItem } from '@/api/exam'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()

const loading = ref(false)
const exams = ref<ExamItem[]>([])

const now = new Date()
const current = reactive({ year: now.getFullYear(), month: now.getMonth() + 1 })

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const currentMonth = computed(() => `${current.year}年${current.month}月`)

/** 当月格子:前置空位 + 每天(带当天考试) */
const calendarDays = computed(() => {
  const daysInMonth = new Date(current.year, current.month, 0).getDate()
  const firstDayOfWeek = new Date(current.year, current.month - 1, 1).getDay()

  const cells: ({ date: number; exams: ExamItem[] } | null)[] = []
  for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)

  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${current.year}-${String(current.month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({ date: d, exams: exams.value.filter(e => e.examDate === dateStr) })
  }
  return cells
})

async function fetchData() {
  const monthStr = `${current.year}-${String(current.month).padStart(2, '0')}`
  loading.value = true
  try {
    exams.value = await getExamCalendar(monthStr)
  } finally {
    loading.value = false
  }
}

function changeMonth(delta: number) {
  current.month += delta
  if (current.month > 12) { current.month = 1; current.year++ }
  else if (current.month < 1) { current.month = 12; current.year-- }
  fetchData()
}

function backToToday() {
  current.year = now.getFullYear()
  current.month = now.getMonth() + 1
  fetchData()
}

function goToList() {
  router.push('/exam')
}

onMounted(fetchData)
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="EXAM CALENDAR"
      title="考试日历"
      description="按月查看考试安排,红色待考、绿色已结束,点击可回到考试安排列表核对细节。"
      icon="📅"
      tone="orange"
      :metrics="[{ label: '本月考试', value: exams.length, hint: '场' }]"
    />

    <el-card shadow="never">
      <div class="toolbar">
        <span class="card-title">{{ currentMonth }}</span>
        <div class="toolbar-right">
          <el-button size="small" @click="changeMonth(-1)">◀ 上月</el-button>
          <el-button size="small" type="primary" @click="backToToday">今天</el-button>
          <el-button size="small" @click="changeMonth(1)">下月 ▶</el-button>
        </div>
      </div>

      <div v-loading="loading">
        <div class="calendar-grid">
          <div class="week-header">
            <div v-for="day in weekDays" :key="day" class="week-day">{{ day }}</div>
          </div>

          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            class="calendar-day"
            :class="{ 'empty-day': !day, 'has-exam': day && day.exams.length > 0 }"
          >
            <template v-if="day">
              <div class="day-number">{{ day.date }}</div>
              <div class="day-exams">
                <div
                  v-for="exam in day.exams"
                  :key="exam.id"
                  class="day-exam"
                  :class="exam.status === '待考试' ? 'exam-pending' : 'exam-completed'"
                  :title="`${exam.courseName} ${exam.examTime || ''} ${exam.classroom || ''}`"
                  @click="goToList"
                >
                  {{ exam.courseName }}
                </div>
              </div>
            </template>
          </div>
        </div>

        <el-empty v-if="!loading && !exams.length" description="本月暂无考试安排" :image-size="80" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2350;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.week-header {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.week-day {
  padding: 8px 4px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  color: #6a628c;
  background: #f4effd;
  border-radius: 6px;
}

.calendar-day {
  min-height: 80px;
  padding: 6px 4px;
  background: #faf8fe;
  border-radius: 8px;
  border: 1px solid #ece4fb;
  transition: border-color 0.2s;
}

.calendar-day:hover {
  border-color: #7c5cd6;
}

.empty-day {
  background: transparent;
  border-color: transparent;
}

.has-exam {
  background: #f4effd;
  border-color: #bfaeee;
}

.day-number {
  font-size: 14px;
  font-weight: 500;
  color: #2c2350;
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
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: transform 0.15s;
}

.day-exam:hover {
  transform: scale(1.03);
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
