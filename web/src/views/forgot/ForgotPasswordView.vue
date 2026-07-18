<template>
  <div class="forgot-container">
    <el-card class="forgot-card">
      <h2 class="title">重置密码</h2>
      <p class="subtitle">验证身份后重新设置登录密码</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="学号/工号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入学号（学生）或工号（教师）"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入绑定的手机号"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input
              v-model="form.code"
              placeholder="请输入验证码"
              style="flex: 1"
            />
            <el-button
              :disabled="smsCountdown > 0 || loading"
              @click="handleSendSms"
              style="width: 140px; flex-shrink: 0"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleSubmit"
            style="width: 100%"
          >
            重置密码
          </el-button>
        </el-form-item>

        <div class="login-link">
          <span>想起密码了？</span>
          <el-link type="primary" @click="goToLogin">返回登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendSmsCodeApi, forgotPasswordApi } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const smsCountdown = ref(0)

const form = reactive({
  username: '',
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
})

// 校验确认密码是否一致
const validateConfirmPassword = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { min: 6, max: 20, message: '学号/工号长度为 6-20 位', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

// ===== 发送验证码（复用 1.3，type='FORGOT'） =====
async function handleSendSms() {
  // 先校验手机号
  if (!form.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    await sendSmsCodeApi({
      phone: form.phone,
      type: 'FORGOT', // ✅ 重置密码场景
    })
    ElMessage.success('验证码已发送')

    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e.message || '发送验证码失败')
  }
}

// ===== 提交重置 =====
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const { confirmPassword, ...submitData } = form
      const res = await forgotPasswordApi(submitData)
      if (res.code === 200) {
        ElMessage.success('密码重置成功！请使用新密码登录')
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '重置失败，请检查信息')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '重置异常，请检查后端服务')
    } finally {
      loading.value = false
    }
  })
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.forgot-card {
  width: 480px;
  padding: 30px 40px 20px;
  border-radius: 12px;
}
.title {
  text-align: center;
  margin-bottom: 4px;
  color: #333;
}
.subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 24px;
}
.login-link {
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}
.login-link .el-link {
  font-size: 14px;
}
</style>