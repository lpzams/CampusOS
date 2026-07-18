<script setup>
/**
 * 登录页 —— 微信授权登录（小程序主要登录方式）
 *
 * 备用：账号密码登录（用于测试或特殊场景）
 *
 * 【新增功能时】加其他登录方式照抄本文件即可。
 */
import { ref } from 'vue'
import { wechatLoginApi, loginApi } from '@/api/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)

// ===== 微信授权登录 =====
async function handleWechatLogin() {
  if (loading.value) return
  loading.value = true
  uni.showLoading({ title: '登录中...' })

  try {
    // 1. 获取微信 code
    const loginRes = await new Promise((resolve, reject) => {
      wx.login({
        success: resolve,
        fail: reject,
      })
    })
    const { code } = loginRes

    // 2. 调用后端登录接口（不传 userInfo，由后端从微信获取）
    const result = await wechatLoginApi({ code })

    // 3. 保存登录态
    uni.setStorageSync('token', result.token)
    uni.setStorageSync('userInfo', JSON.stringify(result))

    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })

    // 4. 跳转首页（tab 页用 switchTab）
    uni.switchTab({ url: '/pages/index/index' })
  } catch (err) {
    uni.hideLoading()
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ===== 账号密码登录（备用） =====
async function handleAccountLogin() {
  if (!username.value || !password.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (loading.value) return
  loading.value = true
  uni.showLoading({ title: '登录中...' })

  try {
    const result = await loginApi({
      username: username.value,
      password: password.value,
    })
    uni.setStorageSync('token', result.token)
    uni.setStorageSync('userInfo', JSON.stringify(result))

    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    uni.switchTab({ url: '/pages/index/index' })
  } catch (err) {
    uni.hideLoading()
    uni.showToast({ title: '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="login-container">
    <view class="logo">🏫 智慧校园门户</view>
    <text class="subtitle">微信授权登录</text>

    <!-- 微信授权登录按钮 -->
    <button
      class="wechat-btn"
      :disabled="loading"
      @click="handleWechatLogin"
    >
      {{ loading ? '登录中...' : '微信一键登录' }}
    </button>

    <!-- 备用：账号密码登录 -->
    <view class="divider">
      <text>其他方式</text>
    </view>
    <view class="form">
      <input
        v-model="username"
        class="input"
        placeholder="学号/工号"
        confirm-type="next"
      />
      <input
        v-model="password"
        type="password"
        class="input"
        placeholder="密码"
        confirm-type="done"
        @confirm="handleAccountLogin"
      />
      <button class="login-btn" :disabled="loading" @click="handleAccountLogin">
        账号密码登录
      </button>
    </view>
  </view>
</template>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.logo {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12rpx;
}
.subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 28rpx;
  margin-bottom: 60rpx;
}
.wechat-btn {
  width: 80%;
  background: #07c160;
  color: #fff;
  border-radius: 50rpx;
  padding: 24rpx 0;
  font-size: 32rpx;
  border: none;
}
.wechat-btn::after {
  border: none;
}
.divider {
  display: flex;
  align-items: center;
  width: 80%;
  margin: 48rpx 0 32rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 24rpx;
}
.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 20rpx;
}
.form {
  width: 80%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.input {
  width: 100%;
  padding: 20rpx 30rpx;
  border-radius: 50rpx;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  font-size: 28rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
}
.login-btn {
  width: 100%;
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  border-radius: 50rpx;
  padding: 20rpx 0;
  font-size: 28rpx;
  border: none;
  margin-top: 10rpx;
}
.login-btn::after {
  border: none;
}
</style>