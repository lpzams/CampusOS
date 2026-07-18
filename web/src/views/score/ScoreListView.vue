<template>
  <div class="score-list-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📊 成绩列表</span>
          <div class="toolbar-right">
            <span class="info-text">学期：{{ data?.semester || '-' }}</span>
            <span class="info-text">GPA：{{ data?.gpa?.toFixed(2) || '-' }}</span>
            <span class="info-text">已修学分：{{ data?.passedCredits || 0 }} / {{ data?.totalCredits || 0 }}</span>
          </div>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="query.semester"
          placeholder="全部学期"
          clearable
          style="width: 180px"
          @change="handleSearch"
        >
          <el-option label="2024-2025-1" value="2024-2025-1" />
          <el-option label="2024-2025-2" value="2024-2025-2" />
          <el-option label="2023-2024-1" value="2023-2024-1" />
          <el-option label="2023-2024-2" value="2023-2024-2" />
        </el-select>
        <el-select
          v-model="query.type"
          placeholder="全部类型"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option label="考试" value="EXAM" />
          <el-option label="平时" value="USUAL" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 成绩表格 -->
      <div v-loading="loading">
        <el-empty v-if="!loading && !scores.length" description="暂无成绩数据" />

        <el-table v-else :data="scores" stripe style="width: 100%">
          <el-table-column prop="courseName" label="课程名称" min-width="160" />
          <el-table-column prop="courseCode" label="课程代码" width="140" />
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column prop="score" label="成绩" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: getScoreColor(row.score) }">
                {{ row.score }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="等级" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getGradeTagType(row.grade)" size="small">
                {{ row.grade }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="90" align="center" />
          <el-table-column prop="examTime" label="考试时间" width="120" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isPassed ? 'success' : 'danger'" size="small">
                {{ row.isPassed ? '通过' : '未通过' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getScoreList } from '@/api/score'
import type { ScoreItem, ScoreListResponse } from '@/api/types'

const loading = ref(false)
const data = ref<ScoreListResponse | null>(null)

const query = reactive({
  semester: '',
  type: '' as '' | 'EXAM' | 'USUAL',
})

const scores = computed(() => data.value?.scores || [])

function getScoreColor(score: number): string {
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#409EFF'
  if (score >= 70) return '#E6A23C'
  if (score >= 60) return '#909399'
  return '#F56C6C'
}

const gradeTagMap = {
  '优秀': 'success',
  '良好': 'primary',
  '中等': 'warning',
  '及格': 'info',
  '不及格': 'danger',
} as const

function getGradeTagType(grade: string) {
  return gradeTagMap[grade as keyof typeof gradeTagMap] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {}
    if (query.semester) params.semester = query.semester
    if (query.type) params.type = query.type
    const res = await getScoreList(Object.keys(params).length ? params : undefined)
    if (res.code === 200 && res.data) {
      data.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  query.semester = ''
  query.type = ''
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.score-list-page {
  max-width: 1100px;
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
  gap: 16px;
  color: #909399;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>