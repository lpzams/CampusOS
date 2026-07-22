<script setup>
import { reactive, ref } from 'vue'
import { forgotPassword, login, register, sendSms, smsLogin, wechatLogin } from '@/api/campus'

const tab = ref('login')
const busy = ref(false)
const form = reactive({ username: '', password: '', phone: '', code: '', realName: '', email: '', userType: 1, department: '', studentId: '', newPassword: '' })

function saveSession(data) {
  uni.setStorageSync('token', data.token)
  uni.setStorageSync('user', data)
  uni.showToast({ title: '登录成功' })
  setTimeout(() => uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/index/index' }) }), 400)
}

async function submit() {
  busy.value = true
  try {
    if (tab.value === 'login') saveSession(await login({ username: form.username, password: form.password }))
    if (tab.value === 'sms') saveSession(await smsLogin({ phone: form.phone, code: form.code }))
    if (tab.value === 'register') {
      await register({ username: form.username, password: form.password, realName: form.realName, phone: form.phone, email: form.email, userType: form.userType, department: form.department, studentId: form.studentId })
      uni.showToast({ title: '注册成功' }); tab.value = 'login'
    }
    if (tab.value === 'forgot') {
      await forgotPassword({ username: form.username, phone: form.phone, code: form.code, newPassword: form.newPassword })
      uni.showToast({ title: '密码已重置' }); tab.value = 'login'
    }
  } finally { busy.value = false }
}

async function sendCode() {
  const data = await sendSms({ phone: form.phone, type: tab.value === 'register' ? 'REGISTER' : 'LOGIN' })
  uni.showModal({ title: '验证码已发送', content: data?.code ? `开发验证码：${data.code}` : '请查收手机短信', showCancel: false })
}

function wxLogin() {
  uni.login({ provider: 'weixin', success: async ({ code }) => saveSession(await wechatLogin({ code, userInfo: {} })) })
}
</script>

<template>
  <view class="auth-page">
    <view class="brand"><text class="brand-name">CampusOS</text><text class="brand-sub">高校智慧校园门户 · 统一认证</text></view>
    <view class="auth-card">
      <view class="tabs">
        <view v-for="item in [['login','账号'],['sms','短信'],['register','注册'],['forgot','找回']]" :key="item[0]" class="tab" :class="{ active: tab === item[0] }" @click="tab=item[0]">{{ item[1] }}</view>
      </view>
      <view class="form">
        <input v-if="tab !== 'sms'" v-model="form.username" class="field" placeholder="学号 / 工号 / 用户名" />
        <input v-if="tab === 'login' || tab === 'register'" v-model="form.password" class="field" password placeholder="密码" />
        <input v-if="tab !== 'login'" v-model="form.phone" class="field" type="number" maxlength="11" placeholder="手机号" />
        <view v-if="tab === 'sms' || tab === 'forgot'" class="code-row"><input v-model="form.code" class="field" type="number" placeholder="验证码" /><button class="secondary-btn" @click="sendCode">获取验证码</button></view>
        <template v-if="tab === 'register'">
          <input v-model="form.realName" class="field" placeholder="真实姓名" /><input v-model="form.studentId" class="field" placeholder="学号 / 工号" />
          <input v-model="form.department" class="field" placeholder="院系" /><input v-model="form.email" class="field" placeholder="邮箱" />
          <picker :range="['学生','教师']" @change="form.userType=Number($event.detail.value)+1"><view class="field picker">用户类型：{{ form.userType === 1 ? '学生' : '教师' }}</view></picker>
        </template>
        <input v-if="tab === 'forgot'" v-model="form.newPassword" class="field" password placeholder="新密码" />
        <button class="primary-btn" :loading="busy" @click="submit">{{ {login:'登录',sms:'验证码登录',register:'注册',forgot:'重置密码'}[tab] }}</button>
        <button v-if="tab === 'login'" class="wx-btn" @click="wxLogin">微信授权登录</button>
        <text v-if="tab === 'login'" class="hint">演示账号：admin / 123456（管理员）</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.auth-page { display: flex; align-items: center; flex-direction: column; justify-content: center; min-height: 100vh; padding: 48rpx; background: linear-gradient(135deg, #241457 0%, #7c5cd6 58%, #b06fd0 100%); }
.brand { margin-bottom: 40rpx; text-align: center; }
.brand-name, .brand-sub { display: block; }
.brand-name { color: #fff; font-size: 52rpx; font-weight: 800; letter-spacing: 1rpx; }
.brand-sub { margin-top: 10rpx; color: rgba(255,255,255,.78); font-size: 24rpx; }
.auth-card { width: 100%; overflow: hidden; border: 1rpx solid rgba(255,255,255,.55); border-radius: 26rpx; background: rgba(255,255,255,.97); box-shadow: 0 18rpx 50rpx rgba(36,20,87,.3); }
.tabs { margin-bottom: 0; }
.form { padding: 36rpx 40rpx 40rpx; }
.picker { line-height: 76rpx; color: $campus-text-secondary; }
.code-row { display: flex; gap: 14rpx; }
.code-row .field { flex: 1; min-width: 0; }
.code-row .secondary-btn { flex: 0 0 210rpx; }
.wx-btn { margin-top: 18rpx; border: 0; border-radius: 18rpx; background: #07c160; color: #fff; font-size: 28rpx; }
.wx-btn::after { border: 0; }
.hint { display: block; margin-top: 20rpx; color: $campus-text-muted; font-size: 22rpx; text-align: center; }
</style>
