<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">校园智慧门户</h2>

      <!-- ===== Tab 切换 ===== -->
      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 1.1 密码登录 Tab -->
        <el-tab-pane label="密码登录" name="account">
          <el-form
            ref="accountFormRef"
            :model="accountForm"
            :rules="accountRules"
            @keyup.enter="handleAccountLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="accountForm.username"
                placeholder="学号/工号"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="accountForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleAccountLogin"
                style="width:100%"
              >
                登 录
              </el-button>
            </el-form-item>

            <!-- ✅ 1.5 + 1.6 新增：底部链接（注册 + 忘记密码） -->
            <el-form-item>
              <div class="bottom-links">
                <span>还没有账号？</span>
                <el-link type="primary" @click="goToRegister">立即注册</el-link>
                <span class="divider">|</span>
                <el-link type="primary" @click="goToForgotPassword">忘记密码</el-link>
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 1.2 短信验证码登录 Tab -->
        <el-tab-pane label="验证码登录" name="sms">
          <el-form
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            @keyup.enter="handleSmsLogin"
          >
            <el-form-item prop="phone">
              <el-input
                v-model="smsForm.phone"
                placeholder="手机号"
                prefix-icon="Phone"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div style="display: flex; gap: 10px; width: 100%">
                <el-input
                  v-model="smsForm.code"
                  placeholder="验证码"
                  prefix-icon="Message"
                  size="large"
                  style="flex: 1"
                />
                <el-button
                  size="large"
                  :disabled="smsCountdown > 0"
                  @click="handleSendSms"
                  style="width: 120px; flex-shrink: 0"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleSmsLogin"
                style="width:100%"
              >
                登 录
              </el-button>
            </el-form-item>

            <!-- ✅ 1.5 + 1.6 新增：底部链接（注册 + 忘记密码） -->
            <el-form-item>
              <div class="bottom-links">
                <span>还没有账号？</span>
                <el-link type="primary" @click="goToRegister">立即注册</el-link>
                <span class="divider">|</span>
                <el-link type="primary" @click="goToForgotPassword">忘记密码</el-link>
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendSmsCodeApi } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// ===== 1.1 密码登录相关 =====
const accountFormRef = ref<FormInstance>()
const accountForm = reactive({
  username: '',
  password: '',
})
const accountRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ===== 1.2 短信登录相关 =====
const smsFormRef = ref<FormInstance>()
const smsForm = reactive({
  phone: '',
  code: '',
})
const smsRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

// ===== 通用状态 =====
const activeTab = ref('account') // 默认显示密码登录
const loading = ref(false)
const smsCountdown = ref(0)

// ===== 1.1 密码登录 =====
async function handleAccountLogin() {
  if (!accountFormRef.value) return
  await accountFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const ok = await userStore.login(accountForm.username, accountForm.password)
      if (ok) {
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error('账号或密码错误')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '登录异常，请检查后端服务')
    } finally {
      loading.value = false
    }
  })
}

// ===== 1.2 短信验证码登录 =====
async function handleSmsLogin() {
  if (!smsFormRef.value) return
  await smsFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const ok = await userStore.loginBySms(smsForm.phone, smsForm.code)
      if (ok) {
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error('验证码错误或已过期')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '登录异常，请检查后端服务')
    } finally {
      loading.value = false
    }
  })
}

// ===== 1.3 发送验证码 =====
async function handleSendSms() {
  if (!smsForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    await sendSmsCodeApi({
      phone: smsForm.phone,
      type: 'LOGIN',
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

// ===== ✅ 1.5 新增：跳转注册页 =====
function goToRegister() {
  router.push('/register')
}

// ===== ✅ 1.6 新增：跳转忘记密码页 =====
function goToForgotPassword() {
  router.push('/forgot-password')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 30px 30px 20px;
  border-radius: 12px;
}
.title {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

/* ✅ 1.5 + 1.6 新增：底部链接样式 */
.bottom-links {
  text-align: center;
  margin-top: -8px;
  font-size: 14px;
  color: #606266;
}
.bottom-links .el-link {
  font-size: 14px;
}
.bottom-links .divider {
  margin: 0 8px;
  color: #dcdfe6;
}
</style>