<template>
  <div class="utility-page">
    <el-row :gutter="20">
      <!-- 余额概览 -->
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>
            <span class="title">💰 余额概览</span>
          </template>

          <div v-loading="loading">
            <div v-if="utility" class="balance-overview">
              <div class="balance-item">
                <span class="balance-label">⚡ 电费</span>
                <span class="balance-value" :class="utility.electricityBalance < 20 ? 'text-warning' : ''">
                  ¥{{ utility.electricityBalance.toFixed(2) }}
                </span>
              </div>
              <div class="balance-item">
                <span class="balance-label">💧 水费</span>
                <span class="balance-value" :class="utility.waterBalance < 10 ? 'text-warning' : ''">
                  ¥{{ utility.waterBalance.toFixed(2) }}
                </span>
              </div>
              <div class="update-time">
                更新于 {{ formatDateTime(utility.lastUpdateTime) }}
              </div>
            </div>
            <el-empty v-else description="暂无数据" />
          </div>
        </el-card>
      </el-col>

      <!-- 用电历史 -->
      <el-col :xs="24" :md="16">
        <el-card>
          <template #header>
            <span class="title">📊 用电历史</span>
          </template>

          <div v-loading="loading">
            <el-empty v-if="!loading && !utility?.electricityHistory?.length" description="暂无历史数据" />

            <div v-else class="history-chart">
              <div class="chart-bars">
                <div
                  v-for="(item, index) in utility?.electricityHistory || []"
                  :key="index"
                  class="bar-wrapper"
                >
                  <div
                    class="bar"
                    :style="{
                      height: (item.consumption / maxConsumption * 120) + 'px',
                      backgroundColor: getBarColor(item.consumption),
                    }"
                  >
                    <span class="bar-value">{{ item.consumption.toFixed(1) }}°</span>
                  </div>
                  <div class="bar-label">{{ item.month }}</div>
                  <div class="bar-cost">¥{{ item.cost.toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getUtilityInfo } from '@/api/dormitory'
import type { UtilityResponse } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const utility = ref<UtilityResponse | null>(null)

const maxConsumption = computed(() => {
  const history = utility.value?.electricityHistory || []
  if (!history.length) return 50
  return Math.max(...history.map(h => h.consumption)) * 1.2
})

function getBarColor(consumption: number): string {
  if (consumption > 60) return '#f56c6c'
  if (consumption > 40) return '#e6a23c'
  return '#409eff'
}

async function fetchUtility() {
  loading.value = true
  try {
    const res = await getUtilityInfo()
    if (res.code === 200 && res.data) {
      utility.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchUtility)
</script>

<style scoped>
.utility-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

/* ===== 余额概览 ===== */
.balance-overview {
  padding: 8px 0;
}

.balance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.balance-item:last-of-type {
  border-bottom: none;
}

.balance-label {
  font-size: 15px;
  color: #606266;
}

.balance-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

.text-warning {
  color: #e6a23c;
}

.update-time {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
}

/* ===== 用电历史 ===== */
.history-chart {
  padding: 20px 0;
  overflow-x: auto;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 24px;
  min-height: 180px;
}

.bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bar {
  width: 44px;
  min-height: 10px;
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
  transition: height 0.5s ease;
}

.bar-value {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.bar-label {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
}

.bar-cost {
  font-size: 12px;
  color: #909399;
}
</style>