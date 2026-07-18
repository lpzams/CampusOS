<template>
  <div class="card-info-page">
    <el-row :gutter="20">
      <!-- 卡信息卡片 -->
      <el-col :xs="24" :md="14">
        <el-card>
          <template #header>
            <div class="header-toolbar">
              <span class="title">💳 校园卡信息</span>
              <span class="card-id">卡号：{{ cardInfo?.cardId || '-' }}</span>
            </div>
          </template>

          <div v-loading="loading">
            <el-empty v-if="!loading && !cardInfo" description="未获取到校园卡信息" />

            <div v-else class="card-info">
              <div class="card-status">
                <span class="status-label">状态</span>
                <el-tag :type="statusTagMap[cardInfo?.statusCode || 'NORMAL']" size="large">
                  {{ cardInfo?.status }}
                </el-tag>
              </div>

              <div class="card-balance">
                <span class="balance-label">余额</span>
                <span class="balance-value">¥{{ cardInfo?.balance?.toFixed(2) || '0.00' }}</span>
              </div>

              <el-divider />

              <el-descriptions :column="2" border>
                <el-descriptions-item label="姓名">
                  {{ cardInfo?.realName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="有效期">
                  {{ cardInfo?.expireTime || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="上次充值">
                  {{ formatDateTime(cardInfo?.lastRechargeTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="上次消费">
                  {{ formatDateTime(cardInfo?.lastConsumeTime) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 操作卡片 -->
      <el-col :xs="24" :md="10">
        <el-card>
          <template #header>
            <span class="title">🔧 卡务操作</span>
          </template>

          <div class="card-actions">
            <!-- 充值 -->
            <el-button type="primary" size="large" @click="openRechargeDialog" style="width: 100%">
              💰 充值
            </el-button>

            <!-- 挂失/解挂 -->
            <el-button
              :type="cardInfo?.statusCode === 'LOST' ? 'success' : 'danger'"
              size="large"
              :loading="actionLoading"
              @click="handleLossToggle"
              style="width: 100%"
            >
              {{ cardInfo?.statusCode === 'LOST' ? '🔓 解挂' : '🔒 挂失' }}
            </el-button>

            <el-button
              type="info"
              plain
              size="large"
              @click="refreshCardInfo"
              style="width: 100%"
            >
              🔄 刷新
            </el-button>

            <div v-if="cardInfo?.statusCode === 'LOST'" class="tip-warning">
              ⚠️ 当前校园卡已挂失，请及时解挂或补办
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 充值弹窗 ===== -->
    <el-dialog v-model="rechargeDialogVisible" title="校园卡充值" width="420px">
      <div class="recharge-form">
        <div class="current-balance">
          当前余额：<span class="balance-text">¥{{ cardInfo?.balance?.toFixed(2) || '0.00' }}</span>
        </div>

        <el-form :model="rechargeForm" label-width="80px">
          <el-form-item label="充值金额">
            <el-input-number
              v-model="rechargeForm.amount"
              :min="10"
              :max="500"
              :step="10"
              placeholder="请输入金额"
              style="width: 100%"
            />
            <span class="tip-text">充值范围：10-500 元</span>
          </el-form-item>

          <el-form-item label="支付方式">
            <el-radio-group v-model="rechargeForm.payMethod">
              <el-radio label="WECHAT">微信支付</el-radio>
              <el-radio label="ALIPAY">支付宝</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="confirmRecharge">
          确认充值
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 充值结果弹窗 ===== -->
    <el-dialog v-model="resultDialogVisible" title="充值结果" width="400px">
      <div v-if="rechargeResult" class="result-content">
        <div class="result-success">
          <span class="result-icon">✅</span>
          <h3>充值成功</h3>
          <div class="result-detail">
            <p><span>充值金额</span> <span>¥{{ rechargeResult.amount.toFixed(2) }}</span></p>
            <p><span>旧余额</span> <span>¥{{ rechargeResult.oldBalance.toFixed(2) }}</span></p>
            <p><span>新余额</span> <span style="color: #67c23a; font-weight: 700;">
              ¥{{ rechargeResult.newBalance.toFixed(2) }}
            </span></p>
            <p><span>订单号</span> <span>{{ rechargeResult.orderId }}</span></p>
          </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCardInfo, lossCard, unlossCard, rechargeCard } from '@/api/card'
import type { CardInfo, CardRechargeResponse, PayMethod } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const actionLoading = ref(false)
const rechargeLoading = ref(false)
const cardInfo = ref<CardInfo | null>(null)

const statusTagMap = {
  NORMAL: 'success',
  LOST: 'danger',
  FROZEN: 'warning',
  EXPIRED: 'info',
} as const

// ===== 充值相关 =====
const rechargeDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const rechargeResult = ref<CardRechargeResponse | null>(null)
const rechargeForm = ref({
  amount: 50,
  payMethod: 'WECHAT' as PayMethod,
})

// ===== 获取卡信息 =====
async function fetchCardInfo() {
  loading.value = true
  try {
    const res = await getCardInfo()
    if (res.code === 200 && res.data) {
      cardInfo.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function refreshCardInfo() {
  fetchCardInfo()
}

// ===== 挂失/解挂 =====
async function handleLossToggle() {
  const isLost = cardInfo.value?.statusCode === 'LOST'

  const confirmMsg = isLost
    ? '确定要解挂校园卡吗？解挂后卡片将恢复正常使用。'
    : '确定要挂失校园卡吗？挂失后卡片将被冻结，无法使用。'

  try {
    await ElMessageBox.confirm(confirmMsg, isLost ? '解挂确认' : '挂失确认', {
      type: isLost ? 'warning' : 'error',
      confirmButtonText: isLost ? '确定解挂' : '确定挂失',
    })
  } catch {
    return
  }

  actionLoading.value = true
  try {
    const res = isLost ? await unlossCard() : await lossCard()
    if (res.code === 200 && res.data) {
      ElMessage.success(res.msg)
      await fetchCardInfo()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败，请重试')
  } finally {
    actionLoading.value = false
  }
}

// ===== 充值弹窗 =====
function openRechargeDialog() {
  rechargeForm.value.amount = 50
  rechargeDialogVisible.value = true
}

async function confirmRecharge() {
  if (!rechargeForm.value.amount || rechargeForm.value.amount < 10) {
    ElMessage.warning('请输入有效的充值金额（10-500元）')
    return
  }

  rechargeLoading.value = true
  try {
    const res = await rechargeCard({
      amount: rechargeForm.value.amount,
      payMethod: rechargeForm.value.payMethod,
    })
    if (res.code === 200 && res.data) {
      rechargeResult.value = res.data
      rechargeDialogVisible.value = false
      resultDialogVisible.value = true
      await fetchCardInfo()
    } else {
      ElMessage.error(res.msg || '充值失败')
    }
  } catch {
    ElMessage.error('充值失败，请重试')
  } finally {
    rechargeLoading.value = false
  }
}

// ===== 生命周期 =====
onMounted(fetchCardInfo)
</script>

<style scoped>
.card-info-page {
  max-width: 1100px;
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

.card-id {
  font-size: 13px;
  color: #909399;
}

/* ===== 卡信息 ===== */
.card-info {
  padding: 8px 0;
}

.card-status {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.status-label {
  font-size: 14px;
  color: #606266;
}

.card-balance {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.balance-label {
  font-size: 14px;
  color: #606266;
}

.balance-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}

/* ===== 操作按钮 ===== */
.card-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.tip-warning {
  margin-top: 8px;
  padding: 10px 12px;
  background: #fdf6ec;
  border-radius: 6px;
  color: #e6a23c;
  font-size: 13px;
  text-align: center;
}

/* ===== 充值弹窗 ===== */
.recharge-form {
  padding: 8px 0;
}

.current-balance {
  text-align: center;
  font-size: 16px;
  color: #606266;
  margin-bottom: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.balance-text {
  font-size: 22px;
  font-weight: 700;
  color: #409eff;
}

.tip-text {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* ===== 充值结果 ===== */
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

.result-detail {
  text-align: left;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.result-detail p {
  display: flex;
  justify-content: space-between;
  margin: 6px 0;
  font-size: 14px;
  color: #606266;
}

.result-detail p span:first-child {
  color: #909399;
}
</style>