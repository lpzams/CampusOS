<script setup lang="ts">
/**
 * 水电余额(整合自 CampusOS_a 的 UtilityView)。
 *
 * 对应后端 GET /api/dormitory/utility(返回 electricityBalance/waterBalance),
 * 顺带拉宿舍信息展示楼栋房间号;余额偏低给出提醒,可直达电费充值。
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDormitoryInfo, getUtility } from '@/api/dormitory'
import type { DormitoryInfo } from '@/api/dormitory'
import { formatMoney } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()

const loading = ref(false)
const utility = ref<{ electricityBalance: number; waterBalance: number } | null>(null)
const dorm = ref<DormitoryInfo | null>(null)

async function fetchData() {
  loading.value = true
  try {
    utility.value = await getUtility()
    // 宿舍信息拉不到不影响余额展示
    try { dorm.value = await getDormitoryInfo() } catch { /* 忽略 */ }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div v-loading="loading">
    <ServiceHero
      eyebrow="UTILITY BALANCE"
      title="水电余额"
      description="宿舍水电费余额实时查询,电费不足 20 元、水费不足 10 元会标红提醒,别让熄灯打断你的夜晚。"
      icon="💡"
      tone="green"
      :metrics="[{ label: '宿舍', value: dorm ? `${dorm.building}-${dorm.room}` : '-' }]"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/electricity-recharge')">去充电费</el-button>
        <el-button @click="router.push('/dormitory')">宿舍管理</el-button>
      </template>
    </ServiceHero>

    <div class="balance-row">
      <el-card shadow="never" class="balance-card">
        <div class="balance-icon">⚡</div>
        <div class="balance-body">
          <div class="balance-label">电费余额</div>
          <div class="balance-value" :class="{ 'text-warning': (utility?.electricityBalance ?? 0) < 20 }">
            {{ formatMoney(utility?.electricityBalance) }}
          </div>
          <div v-if="(utility?.electricityBalance ?? 0) < 20" class="balance-tip">余额偏低,建议尽快充值</div>
        </div>
        <el-button size="small" type="primary" plain @click="router.push('/electricity-recharge')">充值</el-button>
      </el-card>

      <el-card shadow="never" class="balance-card">
        <div class="balance-icon">💧</div>
        <div class="balance-body">
          <div class="balance-label">水费余额</div>
          <div class="balance-value" :class="{ 'text-warning': (utility?.waterBalance ?? 0) < 10 }">
            {{ formatMoney(utility?.waterBalance) }}
          </div>
          <div v-if="(utility?.waterBalance ?? 0) < 10" class="balance-tip">余额偏低,请到宿管处充值</div>
        </div>
      </el-card>
    </div>

    <el-card v-if="dorm" shadow="never">
      <template #header>
        <span class="card-title">🏠 关联宿舍</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="楼栋">{{ dorm.building }}</el-descriptions-item>
        <el-descriptions-item label="房间">{{ dorm.room }}</el-descriptions-item>
        <el-descriptions-item label="户型">{{ dorm.type || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.balance-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.balance-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}

.balance-icon {
  font-size: 40px;
  line-height: 1;
}

.balance-body {
  flex: 1;
}

.balance-label {
  font-size: 13px;
  color: #a89ec9;
}

.balance-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c2350;
  margin: 2px 0;
}

.text-warning {
  color: #f56c6c;
}

.balance-tip {
  font-size: 12px;
  color: #f56c6c;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c2350;
}

@media (max-width: 640px) {
  .balance-row {
    grid-template-columns: 1fr;
  }
}
</style>
