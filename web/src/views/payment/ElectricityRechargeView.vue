<template>
  <div class="electricity-recharge-page">
    <el-card>
      <template #header>
        <span class="title">⚡ 电费充值</span>
      </template>

      <div class="recharge-container">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          size="large"
          style="max-width: 500px; margin: 0 auto;"
        >
          <el-form-item label="宿舍号" prop="dormitoryId">
            <el-input
              v-model="form.dormitoryId"
              placeholder="请输入宿舍号，如：6-302"
            />
          </el-form-item>

          <el-form-item label="充值金额" prop="amount">
            <el-input-number
              v-model="form.amount"
              :min="10"
              :max="500"
              :step="10"
              placeholder="请输入充值金额"
              style="width: 100%"
            />
            <span class="tip-text">充值金额范围：10-500 元</span>
          </el-form-item>

          <el-form-item label="支付方式" prop="payMethod">
            <el-radio-group v-model="form.payMethod">
              <el-radio label="WECHAT">微信支付</el-radio>
              <el-radio label="ALIPAY">支付宝</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
              style="width: 100%"
            >
              立即充值
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 充值结果 -->
        <el-dialog v-model="resultDialogVisible" title="充值结果" width="400px">
          <div v-if="result" class="result-content">
            <div class="result-success">
              <span class="result-icon">✅</span>
              <h3>充值成功</h3>
              <div class="result-detail">
                <p><span>宿舍号</span> <span>{{ result.dormitoryId }}</span></p>
                <p><span>充值金额</span> <span>¥{{ result.amount.toFixed(2) }}</span></p>
                <p><span>旧余额</span> <span>¥{{ result.oldBalance.toFixed(2) }}</span></p>
                <p><span>新余额</span> <span style="color: #67c23a; font-weight: 700;">
                  ¥{{ result.newBalance.toFixed(2) }}
                </span></p>
                <p><span>订单号</span> <span>{{ result.orderId }}</span></p>
              </div>
            </div>
          </div>
          <template #footer>
            <el-button type="primary" @click="resultDialogVisible = false">我知道了</el-button>
          </template>
        </el-dialog>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { rechargeElectricity } from '@/api/payment'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElectricityRechargeResponse, PayMethod } from '@/api/types'

const formRef = ref<FormInstance>()
const submitting = ref(false)
const resultDialogVisible = ref(false)
const result = ref<ElectricityRechargeResponse | null>(null)

const form = reactive({
  dormitoryId: '',
  amount: 50 as number | undefined,
  payMethod: 'WECHAT' as PayMethod,
})

const rules: FormRules = {
  dormitoryId: [
    { required: true, message: '请输入宿舍号', trigger: 'blur' },
    { pattern: /^\d+-?\d+$/, message: '请输入正确的宿舍号，如：6-302', trigger: 'blur' },
  ],
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 10, max: 500, message: '充值金额范围为 10-500 元', trigger: 'blur' },
  ],
  payMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' },
  ],
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const res = await rechargeElectricity({
        dormitoryId: form.dormitoryId,
        amount: form.amount!,
        payMethod: form.payMethod,
      })
      if (res.code === 200 && res.data) {
        result.value = res.data
        resultDialogVisible.value = true
        // 重置表单
        form.dormitoryId = ''
        form.amount = 50
      } else {
        ElMessage.error(res.msg || '充值失败')
      }
    } catch {
      ElMessage.error('充值失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.electricity-recharge-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.recharge-container {
  padding: 20px 0;
}

.tip-text {
  font-size: 12px;
  color: #909399;
  display: block;
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