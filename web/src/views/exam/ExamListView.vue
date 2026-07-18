<template>
  <div class="exam-list-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📝 考试安排</span>
          <span class="info-text">共 {{ exams.length }} 场考试</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !exams.length" description="暂无考试安排" />

        <div v-else class="exam-grid">
          <div
            v-for="exam in exams"
            :key="exam.id"
            class="exam-card"
            :class="{ 'exam-completed': exam.statusCode === 'COMPLETED' }"
          >
            <div class="exam-header">
              <div class="exam-course">
                <span class="course-name">{{ exam.courseName }}</span>
                <span class="course-code">{{ exam.courseCode }}</span>
              </div>
              <el-tag :type="exam.statusCode === 'PENDING' ? 'danger' : 'success'" size="small">
                {{ exam.status }}
              </el-tag>
            </div>

            <div class="exam-body">
              <div class="exam-info-item">
                <span class="label">📅 日期</span>
                <span>{{ exam.examDate }}</span>
              </div>
              <div class="exam-info-item">
                <span class="label">⏰ 时间</span>
                <span>{{ exam.examTime }}</span>
              </div>
              <div class="exam-info-item">
                <span class="label">📍 地点</span>
                <span>{{ exam.building }} {{ exam.classroom }}</span>
              </div>
              <div class="exam-info-item">
                <span class="label">💺 座位号</span>
                <span>{{ exam.seatNumber }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getExamList } from '@/api/exam'
import type { ExamItem } from '@/api/types'

const loading = ref(false)
const exams = ref<ExamItem[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getExamList()
    if (res.code === 200 && res.data) {
      exams.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.exam-list-page {
  max-width: 1000px;
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

.info-text {
  color: #909399;
  font-size: 14px;
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.exam-card {
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 10px;
  border-left: 4px solid #f56c6c;
  transition: box-shadow 0.2s;
}

.exam-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.exam-completed {
  border-left-color: #67c23a;
  opacity: 0.8;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.exam-course {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.course-code {
  font-size: 12px;
  color: #909399;
}

.exam-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 16px;
}

.exam-info-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
  color: #606266;
}

.exam-info-item .label {
  color: #909399;
}

@media (max-width: 640px) {
  .exam-grid {
    grid-template-columns: 1fr;
  }
  .exam-body {
    grid-template-columns: 1fr;
  }
}
</style>