<template>
  <div class="pending-payments-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">💰 待缴费</span>
          <span class="info-text">共 {{ pendingList.length }} 项待缴费</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !pendingList.length" description="🎉 暂无待缴费项目" />

        <div v-else class="payment-grid">
          <div
            v-for="item in pendingList"
            :key="item.id"
            class="payment-card"
          >
            <div class="payment-header">
              <div class="payment-type">
                <span class="type-name">{{ item.type }}</span>
                <el-tag :type="item.statusCode === 'OVERDUE' ? 'danger' : 'warning'" size="small">
                  {{ item.status }}
                </el-tag>
              </div>
              <span class="payment-amount">¥{{ item.amount.toFixed(2) }}</span>
            </div>

            <div class="payment-body">
              <div class="payment-info">
                <span class="label">📋 {{ item.description }}</span>
              </div>
              <div class="payment-info">
                <span class="label">⏰ 截止日期</span>
                <span :class="{ 'text-danger': isOverdue(item.deadline) }">
                  {{ item.deadline }}
                </span>
              </div>
              <div v-if="item.lateFee > 0" class="payment-info">
                <span class="label">⚠️ 滞纳金</span>
                <span class="text-danger">¥{{ item.lateFee.toFixed(2) }}</span>
              </div>
            </div>

            <div class="payment-footer">
              <el-button
                type="primary"
                size="small"
                @click="handlePay(item)"
              >
                立即缴费
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="选择支付方式" width="420px">
      <div class="pay-methods">
        <div
          class="pay-method"
          :class="{ active: selectedPayMethod === 'WECHAT' }"
          @click="selectedPayMethod = 'WECHAT'"
        >
          <span class="pay-icon">💚</span>
          <span class="pay-name">微信支付</span>
        </div>
        <div
          class="pay-method"
          :class="{ active: selectedPayMethod === 'ALIPAY' }"
          @click="selectedPayMethod = 'ALIPAY'"
        >
          <span class="pay-icon">💙</span>
          <span class="pay-name">支付宝</span>
        </div>
      </div>

      <div v-if="currentPayment" class="pay-summary">
        <span>缴费金额：</span>
        <span class="pay-amount">¥{{ currentPayment.amount.toFixed(2) }}</span>
      </div>

      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmPay">
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 支付结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="支付结果" width="400px">
      <div class="result-content">
        <div v-if="payResult" class="result-success">
          <span class="result-icon">✅</span>
          <h3>订单创建成功</h3>
          <p>订单号：{{ payResult.orderId }}</p>
          <p>金额：¥{{ payResult.amount.toFixed(2) }}</p>
          <p>请使用 {{ payResult.payMethod }} 扫码支付</p>
          <el-image
            v-if="payResult.qrCode"
            :src="payResult.qrCode"
            style="width: 160px; height: 160px; margin: 12px auto;"
            fit="contain"
          />
          <p class="expire-tip">⏰ 请在 {{ payResult.expireTime }} 前完成支付</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingPayments, createOrder } from '@/api/payment'
import type { PendingPayment, CreateOrderResponse, PayMethod } from '@/api/types'

const loading = ref(false)
const submitting = ref(false)
const pendingList = ref<PendingPayment[]>([])
const payDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const currentPayment = ref<PendingPayment | null>(null)
const selectedPayMethod = ref<PayMethod>('WECHAT')
const payResult = ref<CreateOrderResponse | null>(null)

function isOverdue(deadline: string): boolean {
  return new Date(deadline) < new Date()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getPendingPayments()
    if (res.code === 200 && res.data) {
      pendingList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function handlePay(item: PendingPayment) {
  currentPayment.value = item
  selectedPayMethod.value = 'WECHAT'
  payDialogVisible.value = true
}

async function confirmPay() {
  if (!currentPayment.value) return

  submitting.value = true
  try {
    const res = await createOrder({
      paymentId: currentPayment.value.id,
      payMethod: selectedPayMethod.value,
    })
    if (res.code === 200 && res.data) {
      payResult.value = res.data
      payDialogVisible.value = false
      resultDialogVisible.value = true
      // 刷新待缴费列表
      await fetchData()
    } else {
      ElMessage.error(res.msg || '创建订单失败')
    }
  } catch {
    ElMessage.error('创建订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.pending-payments-page {
  max-width: 1000px;
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

.payment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.payment-card {
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  transition: box-shadow 0.2s;
}

.payment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.payment-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.payment-amount {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.payment-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.payment-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.payment-info .label {
  color: #909399;
}

.text-danger {
  color: #f56c6c;
}

.payment-footer {
  display: flex;
  justify-content: flex-end;
}

/* ===== 支付弹窗 ===== */
.pay-methods {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.pay-method {
  flex: 1;
  padding: 16px;
  text-align: center;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-method:hover {
  border-color: #409eff;
}

.pay-method.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.pay-icon {
  font-size: 28px;
  display: block;
  margin-bottom: 4px;
}

.pay-name {
  font-size: 14px;
  font-weight: 500;
}

.pay-summary {
  text-align: center;
  font-size: 16px;
  color: #606266;
  padding: 12px 0;
}

.pay-summary .pay-amount {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

/* ===== 结果弹窗 ===== */
.result-content {
  text-align: center;
  padding: 12px 0;
}

.result-icon {
  font-size: 48px;
}

.result-content h3 {
  margin: 8px 0;
  color: #303133;
}

.result-content p {
  margin: 4px 0;
  color: #606266;
}

.expire-tip {
  color: #e6a23c;
  font-size: 13px;
  margin-top: 8px;
}
</style>