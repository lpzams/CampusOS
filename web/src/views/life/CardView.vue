<script setup lang="ts">
/**
 * 校园卡服务页（功能 9）。
 *
 * 顶部一张卡片展示余额/状态，下面是操作区（挂失/解挂/充值）和消费记录。
 * 对应后端 /api/card/info、/loss、/unloss、/recharge、/transactions。
 * 状态规则在后端领域对象 CampusCard 里（如挂失后不能充值），前端只管调用。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelLoss, getCardInfo, getCardTransactions, rechargeCard, reportLoss,
} from '@/api/card'
import type { CardInfo, CardTransaction } from '@/api/card'
import { PAY_METHODS } from '@/api/payment'
import { formatDateTime, formatMoney } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const loading = ref(false)
const card = ref<CardInfo | null>(null)
const transactions = ref<CardTransaction[]>([])

async function fetchAll() {
  loading.value = true
  try {
    card.value = await getCardInfo()
    transactions.value = await getCardTransactions()
  } finally {
    loading.value = false
  }
}

async function handleLoss() {
  const ok = await ElMessageBox.confirm('挂失后校园卡将无法消费，确定挂失吗？', '挂失确认', { type: 'warning' })
    .catch(() => false)
  if (!ok) return
  card.value = await reportLoss()
  ElMessage.success('已挂失')
}

async function handleUnloss() {
  card.value = await cancelLoss()
  ElMessage.success('已解挂')
}

// ===== 充值弹窗 =====
const rechargeVisible = ref(false)
const recharging = ref(false)
const rechargeForm = reactive({ amount: 50, payMethod: 'WECHAT' })
const QUICK_AMOUNTS = [20, 50, 100, 200]

async function confirmRecharge() {
  if (!rechargeForm.amount || rechargeForm.amount <= 0) {
    ElMessage.warning('请输入正确的充值金额')
    return
  }
  recharging.value = true
  try {
    card.value = await rechargeCard({ amount: rechargeForm.amount, payMethod: rechargeForm.payMethod })
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    transactions.value = await getCardTransactions()
  } finally {
    recharging.value = false
  }
}

function statusType(status?: string): 'success' | 'danger' | 'info' {
  if (status === '正常') return 'success'
  if (status === '挂失') return 'danger'
  return 'info'
}

onMounted(fetchAll)
</script>

<template>
  <div v-loading="loading">
    <ServiceHero
      eyebrow="ONE CARD"
      title="校园一卡通"
      description="覆盖食堂、商超和校内服务消费，支持在线充值、挂失保护与账单查询。"
      icon="💳"
      :metrics="[
        { label: '当前余额', value: formatMoney(card?.balance || 0) },
        { label: '卡片状态', value: card?.status || '加载中' },
        { label: '本页记录', value: transactions.length },
      ]"
    >
      <template #actions>
        <el-button type="primary" plain :disabled="!card" @click="rechargeVisible = true">立即充值</el-button>
      </template>
    </ServiceHero>
    <!-- 卡片 -->
    <div v-if="card" class="campus-card">
      <div class="card-top">
        <span class="card-label">校园卡</span>
        <el-tag :type="statusType(card.status)" effect="dark" size="small">{{ card.status }}</el-tag>
      </div>
      <div class="card-balance">
        <span class="balance-label">当前余额</span>
        <span class="balance-value">{{ formatMoney(card.balance) }}</span>
      </div>
      <div class="card-bottom">
        <span>卡号 {{ card.cardId }}</span>
        <span>{{ card.realName }}</span>
      </div>
    </div>

    <!-- 操作区 -->
    <el-card shadow="never" class="action-card">
      <el-button type="primary" @click="rechargeVisible = true">充值</el-button>
      <el-button v-if="card?.status === '正常'" type="warning" @click="handleLoss">挂失</el-button>
      <el-button v-else-if="card?.status === '挂失'" type="success" @click="handleUnloss">解挂</el-button>
    </el-card>

    <!-- 消费记录 -->
    <el-card shadow="never">
      <template #header><span class="card-title">消费/充值记录</span></template>
      <el-table :data="transactions" stripe>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="merchant" label="商户/说明" min-width="140">
          <template #default="{ row }">{{ (row as CardTransaction).merchant || '-' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :class="Number((row as CardTransaction).amount) < 0 ? 'amount-out' : 'amount-in'">
              {{ formatMoney((row as CardTransaction).amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatDateTime((row as CardTransaction).createTime) }}</template>
        </el-table-column>
        <template #empty>暂无记录</template>
      </el-table>
    </el-card>

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeVisible" title="校园卡充值" width="420px">
      <p class="method-label">充值金额</p>
      <div class="quick-amounts">
        <el-button
          v-for="a in QUICK_AMOUNTS"
          :key="a"
          :type="rechargeForm.amount === a ? 'primary' : 'default'"
          @click="rechargeForm.amount = a"
        >{{ a }} 元</el-button>
      </div>
      <el-input-number v-model="rechargeForm.amount" :min="1" :max="1000" class="amount-input" />
      <el-divider />
      <p class="method-label">支付方式</p>
      <el-radio-group v-model="rechargeForm.payMethod">
        <el-radio v-for="m in PAY_METHODS" :key="m.code" :value="m.code" border>{{ m.name }}</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="confirmRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.campus-card {
  background: linear-gradient(135deg, #7c5cd6, #b06fd0);
  color: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  max-width: 420px;
  box-shadow: 0 8px 24px rgba(23, 107, 85, 0.25);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-label {
  font-size: 15px;
  letter-spacing: 2px;
}

.card-balance {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-label {
  font-size: 12px;
  opacity: 0.85;
}

.balance-value {
  font-size: 32px;
  font-weight: 700;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  opacity: 0.9;
}

.action-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
}

.amount-out {
  color: #f56c6c;
}

.amount-in {
  color: #67c23a;
}

.method-label {
  margin: 0 0 10px;
  color: #606266;
}

.quick-amounts {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.amount-input {
  width: 100%;
}
</style>
