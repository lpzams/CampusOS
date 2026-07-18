<template>
  <div class="gpa-dashboard">
    <el-row :gutter="20">
      <!-- 概览卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="gpa-card">
          <div class="card-content">
            <div class="card-value">{{ gpaData?.overallGpa?.toFixed(2) || '-' }}</div>
            <div class="card-label">总 GPA</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="gpa-card">
          <div class="card-content">
            <div class="card-value">{{ gpaData?.currentSemesterGpa?.toFixed(2) || '-' }}</div>
            <div class="card-label">本学期 GPA</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="gpa-card">
          <div class="card-content">
            <div class="card-value">{{ gpaData?.passedCredits || 0 }}</div>
            <div class="card-label">已修学分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="gpa-card">
          <div class="card-content">
            <div class="card-value">{{ gpaData?.rank || '-' }} / {{ gpaData?.totalStudents || '-' }}</div>
            <div class="card-label">排名</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- GPA 历史趋势 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span class="title">📈 GPA 历史趋势</span>
      </template>

      <div v-loading="loading" style="min-height: 200px">
        <el-empty v-if="!loading && !gpaData?.history?.length" description="暂无历史数据" />

        <div v-else class="chart-container">
          <div class="chart-bars">
            <div
              v-for="(item, index) in gpaData?.history || []"
              :key="index"
              class="chart-bar-wrapper"
            >
              <div
                class="chart-bar"
                :style="{
                  height: (item.gpa / 5 * 160) + 'px',
                  backgroundColor: getBarColor(item.gpa),
                }"
                :title="`${item.semester}: ${item.gpa.toFixed(2)}`"
              >
                <span class="bar-value">{{ item.gpa.toFixed(2) }}</span>
              </div>
              <div class="bar-label">{{ item.semester }}</div>
              <div class="bar-credit">{{ item.credits }}学分</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGpa } from '@/api/score'
import type { GpaResponse } from '@/api/types'

const loading = ref(false)
const gpaData = ref<GpaResponse | null>(null)

function getBarColor(gpa: number): string {
  if (gpa >= 4.0) return '#67C23A'
  if (gpa >= 3.5) return '#409EFF'
  if (gpa >= 3.0) return '#E6A23C'
  if (gpa >= 2.0) return '#909399'
  return '#F56C6C'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getGpa()
    if (res.code === 200 && res.data) {
      gpaData.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.gpa-dashboard {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.gpa-card {
  text-align: center;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-content {
  display: flex;
  flex-direction: column;
}

.card-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  padding: 20px 0;
  overflow-x: auto;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 30px;
  min-height: 220px;
}

.chart-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chart-bar {
  width: 50px;
  min-height: 20px;
  border-radius: 4px 4px 0 0;
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
  color: #606266;
  margin-top: 8px;
}

.bar-credit {
  font-size: 11px;
  color: #909399;
}
</style>