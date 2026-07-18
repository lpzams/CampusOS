<template>
  <div class="today-courses-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📖 今日课程</span>
          <span class="info-text">{{ todayInfo }}</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !data?.courses?.length" description="今日暂无课程，好好休息吧 😊" />

        <div v-for="course in data?.courses || []" :key="course.id" class="course-item">
          <div class="course-time" :style="{ borderLeftColor: course.color || '#409EFF' }">
            <span class="time">{{ course.timeSlot }}</span>
            <el-tag :type="statusTagMap[course.status]" size="small">
              {{ course.status }}
            </el-tag>
          </div>
          <div class="course-info">
            <h4>{{ course.name }}</h4>
            <div class="meta">
              <span>👨‍🏫 {{ course.teacher }}</span>
              <span>🏫 {{ course.building }} {{ course.classroom }}</span>
              <span>📖 {{ course.weeks }}</span>
              <span>学分 {{ course.credit }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getTodayCourses } from '@/api/course'
import type { TodayCoursesResponse } from '@/api/types'

const loading = ref(false)
const data = ref<TodayCoursesResponse | null>(null)

const statusTagMap = {
  '未开始': 'info',
  '进行中': 'success',
  '已结束': 'warning',
} as const

const todayInfo = computed(() => {
  if (!data.value) return '加载中...'
  const weekMap = ['日', '一', '二', '三', '四', '五', '六']
  return `${data.value.date} 周${weekMap[data.value.dayOfWeek]} 第${data.value.week}周`
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getTodayCourses()
    if (res.code === 200 && res.data) {
      data.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.today-courses-page {
  max-width: 900px;
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
.course-item {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.course-item:last-child {
  border-bottom: none;
}
.course-time {
  flex-shrink: 0;
  width: 140px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.course-time .time {
  font-weight: 600;
  font-size: 16px;
}
.course-info {
  flex: 1;
}
.course-info h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
}
.course-info .meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}
</style>