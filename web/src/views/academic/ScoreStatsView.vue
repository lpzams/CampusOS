<script setup lang="ts">
/**
 * 成绩统计分析(整合自 CampusOS_a 的 GPADashboard + ScoreStatistics)。
 *
 * 数据全部来自后端 GET /api/score/list:
 *   - 整体统计(GPA/学分/平均分/门数)直接用返回值;
 *   - 各学期 GPA 趋势:按学期再查一次 /score/list,用后端算好的 GPA,不在前端重算公式;
 *   - 成绩分布:按分数段(优秀/良好/中等/及格/不及格)对明细分桶。
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getScores } from '@/api/score'
import type { ScoreSummary } from '@/api/score'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()

const loading = ref(false)
const overall = ref<ScoreSummary | null>(null)

interface SemesterStat {
  semester: string
  gpa: number
  average: number
  totalCredits: number
  count: number
}

const semesterStats = ref<SemesterStat[]>([])

/** 分数 -> 分布桶 */
const DIST_BUCKETS = [
  { label: '优秀 (≥90)', min: 90, max: 101, color: '#67C23A' },
  { label: '良好 (80-89)', min: 80, max: 90, color: '#409EFF' },
  { label: '中等 (70-79)', min: 70, max: 80, color: '#E6A23C' },
  { label: '及格 (60-69)', min: 60, max: 70, color: '#909399' },
  { label: '不及格 (<60)', min: -1, max: 60, color: '#F56C6C' },
]

const distribution = computed(() => {
  const scores = overall.value?.scores ?? []
  return DIST_BUCKETS.map(bucket => ({
    ...bucket,
    count: scores.filter(s => s.score >= bucket.min && s.score < bucket.max).length,
  }))
})

const totalCount = computed(() => overall.value?.scores.length ?? 0)

function percentage(count: number) {
  return totalCount.value === 0 ? 0 : Math.round((count / totalCount.value) * 100)
}

/** GPA 柱状色(与 CampusOS_a GPADashboard 一致) */
function gpaColor(gpa: number): string {
  if (gpa >= 4.0) return '#67C23A'
  if (gpa >= 3.5) return '#409EFF'
  if (gpa >= 3.0) return '#E6A23C'
  if (gpa >= 2.0) return '#909399'
  return '#F56C6C'
}

async function fetchData() {
  loading.value = true
  try {
    const summary = await getScores()
    overall.value = summary

    // 各学期统计:去重后逐学期取后端算好的 GPA/平均分
    const semesters = [...new Set(summary.scores.map(s => String(s.semester || '')).filter(Boolean))].sort()
    const results = await Promise.all(semesters.map(semester => getScores({ semester })))
    semesterStats.value = results.map((r, i) => ({
      semester: semesters[i],
      gpa: r.gpa,
      average: r.average,
      totalCredits: r.totalCredits,
      count: r.count,
    }))
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div v-loading="loading">
    <ServiceHero
      eyebrow="SCORE ANALYTICS"
      title="成绩统计分析"
      description="GPA 趋势、成绩分布、学期对比,一页看清自己的学业曲线。"
      icon="📊"
      tone="blue"
      :metrics="[
        { label: '总 GPA', value: overall?.gpa?.toFixed(2) ?? '-' },
        { label: '总学分', value: overall?.totalCredits ?? '-' },
        { label: '课程门数', value: overall?.count ?? '-' },
      ]"
    >
      <template #actions>
        <el-button @click="router.push('/score')">返回成绩查询</el-button>
      </template>
    </ServiceHero>

    <!-- 概览统计卡 -->
    <div class="stat-row">
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ overall?.gpa?.toFixed(2) ?? '-' }}</div>
        <div class="stat-label">平均绩点 GPA</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ overall?.average?.toFixed(1) ?? '-' }}</div>
        <div class="stat-label">平均分</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ overall?.totalCredits ?? '-' }}</div>
        <div class="stat-label">总学分</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-value">{{ overall?.count ?? '-' }}</div>
        <div class="stat-label">课程门数</div>
      </el-card>
    </div>

    <!-- GPA 学期趋势 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">📈 各学期 GPA 趋势</span>
      </template>

      <el-empty v-if="!loading && !semesterStats.length" description="暂无学期数据" :image-size="80" />

      <div v-else class="chart-container">
        <div class="chart-bars">
          <div v-for="item in semesterStats" :key="item.semester" class="chart-bar-wrapper">
            <div
              class="chart-bar"
              :style="{ height: Math.max((item.gpa / 5) * 160, 20) + 'px', backgroundColor: gpaColor(item.gpa) }"
              :title="`${item.semester}: GPA ${item.gpa.toFixed(2)} / 平均分 ${item.average.toFixed(1)}`"
            >
              <span class="bar-value">{{ item.gpa.toFixed(2) }}</span>
            </div>
            <div class="bar-label">{{ item.semester }}</div>
            <div class="bar-credit">{{ item.totalCredits }}学分 · {{ item.count }}门</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 成绩分布 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">🎯 成绩分布</span>
      </template>

      <el-empty v-if="!loading && totalCount === 0" description="暂无成绩数据" :image-size="80" />

      <div v-else class="distribution">
        <div v-for="item in distribution" :key="item.label" class="distribution-item">
          <span class="dist-label">{{ item.label }}</span>
          <el-progress
            class="dist-bar"
            :percentage="percentage(item.count)"
            :color="item.color"
            :stroke-width="18"
          />
          <span class="dist-count">{{ item.count }} 门</span>
        </div>
      </div>
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

.section-card {
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c2350;
}

.chart-container {
  padding: 16px 0;
  overflow-x: auto;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 34px;
  min-height: 220px;
}

.chart-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chart-bar {
  width: 52px;
  border-radius: 6px 6px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
  transition: height 0.5s ease;
}

.bar-value {
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.bar-label {
  font-size: 12px;
  color: #6a628c;
  margin-top: 8px;
}

.bar-credit {
  font-size: 11px;
  color: #a89ec9;
}

.distribution {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 6px 0;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dist-label {
  width: 110px;
  font-size: 13px;
  font-weight: 500;
  color: #2c2350;
  text-align: right;
  flex-shrink: 0;
}

.dist-bar {
  flex: 1;
}

.dist-count {
  width: 46px;
  font-size: 13px;
  color: #a89ec9;
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .dist-label {
    width: 84px;
    font-size: 12px;
  }
}
</style>
