<script setup lang="ts">
/**
 * 考试安排页（功能 7）。
 *
 * 对应后端 GET /api/exam/list（全部考试）。
 * 卡片列表展示，把「待考试」的排在前面并高亮，方便看临近的考试。
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Document, Location, Tickets } from '@element-plus/icons-vue'
import { getExams } from '@/api/exam'
import type { ExamItem } from '@/api/exam'

const router = useRouter()

const loading = ref(false)
const exams = ref<ExamItem[]>([])

/** 待考试的排前面 */
const sortedExams = computed(() =>
  [...exams.value].sort((a, b) => String(a.examDate).localeCompare(String(b.examDate))),
)

async function fetchExams() {
  loading.value = true
  try {
    exams.value = await getExams()
  } finally {
    loading.value = false
  }
}

function statusType(status?: string): 'warning' | 'success' | 'info' {
  if (status === '待考试') return 'warning'
  if (status === '已结束') return 'info'
  return 'success'
}

onMounted(fetchExams)
</script>

<template>
  <div v-loading="loading">
    <el-card shadow="never" class="header-card">
      <span class="page-title">考试安排</span>
      <span class="sub">共 {{ exams.length }} 场考试，请提前查看考场与座位号</span>
      <el-button size="small" class="calendar-btn" :icon="Calendar" @click="router.push('/exam-calendar')">考试日历</el-button>
    </el-card>

    <el-empty v-if="!loading && exams.length === 0" description="暂无考试安排" />

    <div class="exam-grid">
      <el-card v-for="exam in sortedExams" :key="exam.id" shadow="hover" class="exam-card">
        <div class="exam-head">
          <span class="exam-name">{{ exam.courseName }}</span>
          <el-tag size="small" :type="statusType(exam.status)">{{ exam.status || '待考试' }}</el-tag>
        </div>
        <div class="exam-body">
          <div class="exam-row">
            <el-icon><Calendar /></el-icon>
            <span>{{ exam.examDate }} {{ exam.examTime }}</span>
          </div>
          <div class="exam-row">
            <el-icon><Location /></el-icon>
            <span>{{ exam.building }} {{ exam.classroom }}</span>
          </div>
          <div class="exam-row">
            <el-icon><Tickets /></el-icon>
            <span>座位号：{{ exam.seatNumber || '待定' }}</span>
          </div>
          <div v-if="exam.courseCode" class="exam-row">
            <el-icon><Document /></el-icon>
            <span>课程代码：{{ exam.courseCode }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.header-card {
  margin-bottom: 16px;
}

.header-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sub {
  flex: 1;
}

.calendar-btn {
  flex-shrink: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.sub {
  color: #909399;
  font-size: 13px;
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.exam-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.exam-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.exam-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exam-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}
</style>
