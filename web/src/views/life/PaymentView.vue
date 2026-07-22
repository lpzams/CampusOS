<script setup lang="ts">
/**
 * 校园缴费页（功能 8）。
 *
 * 两个 Tab：待缴费（列表 + 立即缴费弹窗选支付方式）、缴费记录。
 * 对应后端 /api/payment/pending、/order、/records。
 * 支付是演示流程：选好支付方式点确认即创建订单，不接真实支付。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  PAY_METHODS, createPaymentOrder, getPaymentRecords, getPendingPayments,
} from '@/api/payment'
import type { PaymentItem, PaymentOrder } from '@/api/payment'
import { formatDateTime, formatMoney } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const activeTab = ref('pending')

const loadingPending = ref(false)
const pending = ref<PaymentItem[]>([])
const loadingRecords = ref(false)
const records = ref<PaymentOrder[]>([])

async function fetchPending() {
  loadingPending.value = true
  try {
    pending.value = await getPendingPayments()
  } finally {
    loadingPending.value = false
  }
}

async function fetchRecords() {
  loadingRecords.value = true
  try {
    records.value = await getPaymentRecords()
  } finally {
    loadingRecords.value = false
  }
}

// ===== 缴费弹窗 =====
const payVisible = ref(false)
const paying = ref(false)
const payForm = reactive({ item: null as PaymentItem | null, payMethod: 'WECHAT' })

function openPay(item: PaymentItem) {
  payForm.item = item
  payForm.payMethod = 'WECHAT'
  payVisible.value = true
}

async function confirmPay() {
  if (!payForm.item) return
  paying.value = true
  try {
    await createPaymentOrder({ paymentId: payForm.item.id, payMethod: payForm.payMethod })
    ElMessage.success('缴费订单已创建')
    payVisible.value = false
    fetchPending()
    fetchRecords()
  } finally {
    paying.value = false
  }
}

function methodName(code?: string) {
  return PAY_METHODS.find(m => m.code === code)?.name ?? code ?? '-'
}

function onTabChange(name: string | number) {
  if (name === 'records' && records.value.length === 0) fetchRecords()
}

onMounted(fetchPending)
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="CAMPUS PAY"
      title="校园缴费"
      description="学费、住宿费和校园生活账单集中处理，缴费状态与历史记录一目了然。"
      icon="¥"
      tone="blue"
      :metrics="[
        { label: '待缴项目', value: pending.length, hint: pending.length ? '请及时处理' : '当前已清空' },
        { label: '待缴金额', value: formatMoney(pending.reduce((sum, item) => sum + Number(item.amount || 0), 0)) },
        { label: '缴费记录', value: records.length },
      ]"
    >
      <template #actions><el-button type="primary" plain @click="fetchPending">刷新账单</el-button></template>
    </ServiceHero>
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 待缴费 -->
        <el-tab-pane label="待缴费" name="pending">
          <div v-loading="loadingPending">
            <el-empty v-if="!loadingPending && pending.length === 0" description="没有待缴费项目" />
            <el-card v-for="item in pending" :key="item.id" shadow="hover" class="pay-card">
              <div class="pay-info">
                <div class="pay-main">
                  <span class="pay-type">{{ item.type }}</span>
                  <span class="pay-desc">{{ item.description }}</span>
                </div>
                <div class="pay-right">
                  <span class="pay-amount">{{ formatMoney(item.amount) }}</span>
                  <span class="pay-deadline">截止 {{ item.deadline }}</span>
                </div>
              </div>
              <template #footer>
                <div class="pay-footer">
                  <el-tag type="warning" size="small">{{ item.status }}</el-tag>
                  <el-button type="primary" size="small" @click="openPay(item)">立即缴费</el-button>
                </div>
              </template>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 缴费记录 -->
        <el-tab-pane label="缴费记录" name="records">
          <el-table v-loading="loadingRecords" :data="records" stripe>
            <el-table-column prop="type" label="项目" min-width="140">
              <template #default="{ row }">{{ (row as PaymentOrder).type || '缴费' }}</template>
            </el-table-column>
            <el-table-column label="金额" width="130">
              <template #default="{ row }">{{ formatMoney((row as PaymentOrder).amount) }}</template>
            </el-table-column>
            <el-table-column label="支付方式" width="120">
              <template #default="{ row }">{{ methodName((row as PaymentOrder).payMethod) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="(row as PaymentOrder).status === '支付成功' ? 'success' : 'info'">
                  {{ (row as PaymentOrder).status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ formatDateTime((row as PaymentOrder).createTime) }}</template>
            </el-table-column>
            <template #empty>暂无缴费记录</template>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 缴费弹窗 -->
    <el-dialog v-model="payVisible" title="确认缴费" width="420px">
      <div v-if="payForm.item" class="pay-dialog">
        <div class="pay-dialog-row"><span>缴费项目</span><span>{{ payForm.item.type }}</span></div>
        <div class="pay-dialog-row"><span>金额</span><span class="amount">{{ formatMoney(payForm.item.amount) }}</span></div>
        <el-divider />
        <p class="method-label">选择支付方式</p>
        <el-radio-group v-model="payForm.payMethod">
          <el-radio v-for="m in PAY_METHODS" :key="m.code" :value="m.code" border>{{ m.name }}</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pay-card {
  margin-bottom: 12px;
}

.pay-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pay-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pay-type {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.pay-desc {
  color: #909399;
  font-size: 13px;
}

.pay-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.pay-amount {
  font-size: 20px;
  font-weight: 700;
  color: #e6a23c;
}

.pay-deadline {
  color: #909399;
  font-size: 12px;
}

.pay-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pay-dialog-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #606266;
}

.pay-dialog-row .amount {
  font-weight: 700;
  color: #e6a23c;
}

.method-label {
  margin: 0 0 10px;
  color: #606266;
}
</style>
