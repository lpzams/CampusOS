<script setup lang="ts">
/**
 * 登录页（功能 1：用户登录与统一认证）。
 *
 * 两种登录方式做成两个 Tab：
 *   账号登录：学号/工号/用户名 + 密码（种子账号 admin / 123456）；
 *   验证码登录：手机号 + 短信验证码（演示模式后端直接把验证码返回，自动帮你填上）。
 * 另有「忘记密码」弹窗（手机验证码重置）。
 *
 * 登录成功：把返回结果写进 stores/user.ts（含 token），
 * 然后回到 ?redirect= 指定的来源页（没有就去资讯首页）。
 */
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { forgotPassword, login, loginBySms, sendSmsCode } from '@/api/auth'
import type { LoginResult } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('account')
const submitting = ref(false)

// ===== 账号密码登录 =====
const accountFormRef = ref<FormInstance>()
const accountForm = reactive({ username: '', password: '' })
const accountRules: FormRules = {
  username: [{ required: true, message: '请输入学号/工号/用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ===== 验证码登录 =====
const smsFormRef = ref<FormInstance>()
const smsForm = reactive({ phone: '', code: '' })
const smsRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

/** 发送验证码后的倒计时（秒），大于 0 时按钮不可点 */
const countdown = ref(0)

async function handleSendCode() {
  // 只校验手机号一个字段
  const valid = await smsFormRef.value?.validateField('phone').catch(() => false)
  if (valid === false) return
  const result = await sendSmsCode(smsForm.phone)
  // 本地演示模式（campus.auth.expose-sms-code=true）后端会把验证码带回来，直接填上方便体验
  if (result.code) {
    smsForm.code = result.code
    ElMessage.success(`验证码已发送（演示模式已自动填入：${result.code}）`)
  } else {
    ElMessage.success('验证码已发送，请注意查收')
  }
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

/** 登录成功后的公共处理 */
function afterLogin(result: LoginResult) {
  userStore.login(result)
  ElMessage.success(`欢迎回来，${result.realName || result.username}`)
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/news'
  router.replace(redirect)
}

async function handleAccountLogin() {
  const valid = await accountFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    afterLogin(await login(accountForm))
  } finally {
    submitting.value = false
  }
}

async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    afterLogin(await loginBySms(smsForm))
  } finally {
    submitting.value = false
  }
}

// ===== 忘记密码弹窗 =====
const forgotVisible = ref(false)
const forgotSubmitting = ref(false)
const forgotFormRef = ref<FormInstance>()
const forgotForm = reactive({ username: '', phone: '', code: '', newPassword: '' })
const forgotRules: FormRules = {
  username: [{ required: true, message: '请输入学号/工号/用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入注册时的手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

async function handleForgotSendCode() {
  const valid = await forgotFormRef.value?.validateField('phone').catch(() => false)
  if (valid === false) return
  const result = await sendSmsCode(forgotForm.phone)
  if (result.code) forgotForm.code = result.code
  ElMessage.success('验证码已发送')
}

async function handleForgotSubmit() {
  const valid = await forgotFormRef.value?.validate().catch(() => false)
  if (!valid) return
  forgotSubmitting.value = true
  try {
    await forgotPassword(forgotForm)
    ElMessage.success('密码已重置，请用新密码登录')
    forgotVisible.value = false
    accountForm.username = forgotForm.username
    accountForm.password = ''
    activeTab.value = 'account'
  } finally {
    forgotSubmitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <router-link to="/" class="brand">CampusOS</router-link>
      <p class="slogan">高校智慧校园门户 · 统一认证</p>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="账号登录" name="account">
          <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-position="top" @submit.prevent>
            <el-form-item label="账号" prop="username">
              <el-input v-model="accountForm.username" placeholder="学号 / 工号 / 用户名" @keyup.enter="handleAccountLogin" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="accountForm.password" type="password" show-password placeholder="请输入密码" @keyup.enter="handleAccountLogin" />
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleAccountLogin">登 录</el-button>
            <p class="hint">演示账号：student / 123456（学生） · teacher / 123456（教师） · admin / 123456（管理员）</p>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="验证码登录" name="sms">
          <el-form ref="smsFormRef" :model="smsForm" :rules="smsRules" label-position="top" @submit.prevent>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="smsForm.phone" maxlength="11" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-row">
                <el-input v-model="smsForm.code" maxlength="6" placeholder="6 位验证码" @keyup.enter="handleSmsLogin" />
                <el-button :disabled="countdown > 0" @click="handleSendCode">
                  {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSmsLogin">登 录</el-button>
            <p class="hint">手机号需已注册（注册页可填写手机号）</p>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="extra-row">
        <el-button link type="primary" @click="router.push('/register')">注册账号</el-button>
        <el-button link @click="forgotVisible = true">忘记密码？</el-button>
      </div>
    </el-card>

    <!-- 忘记密码：手机验证码重置 -->
    <el-dialog v-model="forgotVisible" title="重置密码" width="420px">
      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="forgotForm.username" placeholder="学号 / 工号 / 用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="forgotForm.phone" maxlength="11" placeholder="注册时填写的手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="forgotForm.code" maxlength="6" />
            <el-button @click="handleForgotSendCode">获取验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotVisible = false">取消</el-button>
        <el-button type="primary" :loading="forgotSubmitting" @click="handleForgotSubmit">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #241457 0%, #7c5cd6 55%, #b06fd0 100%);
}

.login-card {
  width: 400px;
  border-radius: 12px;
}

.brand {
  display: block;
  text-align: center;
  font-size: 26px;
  font-weight: 800;
  color: #7c5cd6;
  text-decoration: none;
}

.slogan {
  margin: 6px 0 18px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
}

.hint {
  margin: 10px 0 0;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.extra-row {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}
</style>
