<script setup lang="ts">
/**
 * 电费充值(整合自 CampusOS_a 的 ElectricityRechargeView)。
 *
 * 对应后端 POST /api/payment/electricity(演示流程,下单即成功),
 * 返回充值订单;成功后弹结果对话框并展示订单信息。
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { PAY_METHODS, rechargeElectricity } from '@/api/payment'
import type { PaymentOrder } from '@/api/payment'
import { formatMoney } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const resultVisible = ref(false)
const result = ref<PaymentOrder | null>(null)

const form = reactive({
  dormitoryId: '',
  amount: 50,
  payMethod: 'WECHAT',
})

const rules: FormRules = {
  dormitoryId: [
    { required: true, message: '请输入宿舍号', trigger: 'blur' },
    { pattern: /^\d+-?\d+$/, message: '请输入正确的宿舍号,如:6-302', trigger: 'blur' },
  ],
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 10, max: 500, message: '充值金额范围为 10-500 元', trigger: 'blur' },
  ],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    result.value = await rechargeElectricity({
      dormitoryId: form.dormitoryId,
      amount: form.amount,
      payMethod: form.payMethod,
    })
    resultVisible.value = true
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="ELECTRICITY RECHARGE"
      title="电费充值"
      description="输入宿舍号即可为宿舍充值电费,支持微信与支付宝(演示流程,下单即到账)。"
      icon="⚡"
      tone="orange"
    >
      <template #actions>
        <el-button @click="router.push('/utility')">查看水电余额</el-button>
      </template>
    </ServiceHero>

    <el-card shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        class="recharge-form"
      >
        <el-form-item label="宿舍号" prop="dormitoryId">
          <el-input v-model="form.dormitoryId" placeholder="请输入宿舍号,如:6-302" />
        </el-form-item>

        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="form.amount" :min="10" :max="500" :step="10" style="width: 100%" />
          <span class="tip-text">充值金额范围:10-500 元</span>
        </el-form-item>

        <el-form-item label="支付方式" prop="payMethod">
          <el-radio-group v-model="form.payMethod">
            <el-radio v-for="m in PAY_METHODS" :key="m.code" :value="m.code">{{ m.name }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" class="submit-btn" @click="handleSubmit">
            立即充值
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 充值结果 -->
    <el-dialog v-model="resultVisible" title="充值结果" width="400px">
      <div v-if="result" class="result-content">
        <span class="result-icon">✅</span>
        <h3>充值成功</h3>
        <div class="result-detail">
          <p><span>宿舍号</span><span>{{ result.dormitoryId || form.dormitoryId }}</span></p>
          <p><span>充值金额</span><span>{{ formatMoney(result.amount ?? form.amount) }}</span></p>
          <p><span>支付方式</span><span>{{ PAY_METHODS.find(m => m.code === result?.payMethod)?.name || result.payMethod || '-' }}</span></p>
          <p><span>订单号</span><span>{{ result.id }}</span></p>
          <p v-if="result.status"><span>状态</span><span class="result-ok">{{ result.status }}</span></p>
        </div>
      </div>
      <template #footer>
        <el-button @click="resultVisible = false">继续充值</el-button>
        <el-button type="primary" @click="router.push('/utility')">查看余额</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.recharge-form {
  max-width: 520px;
  margin: 8px auto;
}

.tip-text {
  font-size: 12px;
  color: #a89ec9;
  display: block;
  margin-top: 4px;
}

.submit-btn {
  width: 100%;
}

.result-content {
  text-align: center;
  padding: 8px 0;
}

.result-icon {
  font-size: 48px;
}

.result-content h3 {
  margin: 8px 0;
  color: #2c2350;
}

.result-detail {
  text-align: left;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f4effd;
  border-radius: 8px;
}

.result-detail p {
  display: flex;
  justify-content: space-between;
  margin: 6px 0;
  font-size: 14px;
  color: #6a628c;
}

.result-detail p span:first-child {
  color: #a89ec9;
}

.result-ok {
  color: #67c23a;
  font-weight: 700;
}
</style>
