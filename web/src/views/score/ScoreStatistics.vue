<template>
  <div class="score-statistics">
    <el-card>
      <template #header>
        <span class="title">📊 成绩统计分析</span>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !stats" description="暂无数据" />

        <div v-else class="stats-grid">
          <!-- 概览 -->
          <div class="stats-summary">
            <el-row :gutter="16">
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ stats?.totalCourses || 0 }}</div>
                  <div class="stat-label">总课程数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ stats?.avgScore?.toFixed(1) || '-' }}</div>
                  <div class="stat-label">平均分</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value" style="color: #67C23A">{{ stats?.maxScore || '-' }}</div>
                  <div class="stat-label">最高分</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value" style="color: #F56C6C">{{ stats?.minScore || '-' }}</div>
                  <div class="stat-label">最低分</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 成绩分布 -->
          <el-divider content-position="left">成绩分布</el-divider>
          <div class="distribution">
            <div
              v-for="(count, grade) in stats?.distribution || {}"
              :key="grade"
              class="distribution-item"
            >
              <span class="dist-label">{{ grade }}</span>
              <el-progress
                :percentage="getPercentage(count)"
                :color="getDistColor(grade)"
                :stroke-width="20"
                striped
                striped-flow
              />
              <span class="dist-count">{{ count }}门</span>
            </div>
          </div>

          <!-- 各学期平均分 -->
          <el-divider content-position="left">各学期平均分</el-divider>
          <div class="semester-grid">
            <div
              v-for="item in stats?.bySemester || []"
              :key="item.semester"
              class="semester-item"
            >
              <span class="semester-label">{{ item.semester }}</span>
              <span class="semester-score">{{ item.avgScore.toFixed(1) }}</span>
              <span class="semester-credit">{{ item.credit }}学分</span>
            </div>
          </div>

          <!-- 各类别平均分 -->
          <el-divider content-position="left">各类别平均分</el-divider>
          <div class="category-grid">
            <div
              v-for="(score, category) in stats?.byCategory || {}"
              :key="category"
              class="category-item"
            >
              <span class="category-label">{{ category }}</span>
              <el-progress
                :percentage="(score / 100) * 100"
                :color="getCategoryColor(category)"
                :stroke-width="16"
              />
              <span class="category-score">{{ score.toFixed(1) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getScoreStatistics } from '@/api/score'
import type { ScoreStatisticsResponse } from '@/api/types'

const loading = ref(false)
const stats = ref<ScoreStatisticsResponse | null>(null)

const total = computed(() => {
  const dist = stats.value?.distribution || {}
  return Object.values(dist).reduce((sum, count) => sum + count, 0)
})

function getPercentage(count: number): number {
  if (total.value === 0) return 0
  return Math.round((count / total.value) * 100)
}

const distColorMap: Record<string, string> = {
  '优秀': '#67C23A',
  '良好': '#409EFF',
  '中等': '#E6A23C',
  '及格': '#909399',
  '不及格': '#F56C6C',
}

function getDistColor(grade: string): string {
  return distColorMap[grade] || '#409EFF'
}

function getCategoryColor(category: string): string {
  const colors = ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399']
  const index = category.length % colors.length
  return colors[index]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getScoreStatistics()
    if (res.code === 200 && res.data) {
      stats.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.score-statistics {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.stats-summary {
  padding: 8px 0;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.distribution {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dist-label {
  width: 60px;
  font-size: 14px;
  font-weight: 500;
  text-align: right;
  flex-shrink: 0;
}

.dist-count {
  width: 50px;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.semester-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  padding: 8px 0;
}

.semester-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.semester-label {
  font-size: 13px;
  color: #909399;
}

.semester-score {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.semester-credit {
  font-size: 12px;
  color: #909399;
}

.category-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-label {
  width: 80px;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

.category-score {
  width: 50px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex-shrink: 0;
  text-align: right;
}
</style>