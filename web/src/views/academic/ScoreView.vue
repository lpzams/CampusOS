<script setup lang="ts">
/**
 * 成绩查询页（功能 6）。
 *
 * 对应后端 GET /api/score/list：一次返回统计值（GPA/学分/平均分）+ 成绩明细。
 * 顶部四个统计卡片，下面是成绩表格，可按学期/类型筛选。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getScores } from '@/api/score'
import type { ScoreItem, ScoreSummary } from '@/api/score'

const router = useRouter()

const loading = ref(false)
const data = ref<ScoreSummary | null>(null)

const filter = reactive({ semester: '', type: '' })

const SCORE_TYPES = [
  { code: '', name: '全部' },
  { code: '考试', name: '考试' },
  { code: '平时', name: '平时' },
]

/** 分数 -> 标签颜色 */
function scoreTag(score: number): 'success' | 'warning' | 'danger' {
  if (score >= 85) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

async function fetchScores() {
  loading.value = true
  try {
    data.value = await getScores({
      semester: filter.semester || undefined,
      type: filter.type || undefined,
    })
  } finally {
    loading.value = false
  }
}

onMounted(fetchScores)
</script>

<template>
  <div v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ data?.gpa?.toFixed(2) ?? '-' }}</div>
        <div class="stat-label">平均绩点 GPA</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ data?.totalCredits ?? '-' }}</div>
        <div class="stat-label">总学分</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ data?.average?.toFixed(1) ?? '-' }}</div>
        <div class="stat-label">平均分</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ data?.count ?? '-' }}</div>
        <div class="stat-label">课程门数</div>
      </el-card>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <span class="card-title">成绩明细</span>
        <div class="controls">
          <el-input v-model="filter.semester" placeholder="学期，如 2026-2027-1" clearable class="semester-input" @clear="fetchScores" />
          <el-select v-model="filter.type" class="type-select" @change="fetchScores">
            <el-option v-for="t in SCORE_TYPES" :key="t.code" :label="t.name" :value="t.code" />
          </el-select>
          <el-button type="primary" @click="fetchScores">查询</el-button>
          <el-button @click="router.push('/score-statistics')">统计分析</el-button>
        </div>
      </div>

      <el-table :data="data?.scores ?? []" stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column label="成绩" width="90">
          <template #default="{ row }">
            <el-tag :type="scoreTag((row as ScoreItem).score)" effect="dark">{{ (row as ScoreItem).score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="等级" width="90" />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column prop="examTime" label="考试时间" width="130" />
        <template #empty>暂无成绩数据</template>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #7c5cd6;
}

.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.controls {
  display: flex;
  gap: 8px;
}

.semester-input {
  width: 200px;
}

.type-select {
  width: 110px;
}

@media (max-width: 640px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
