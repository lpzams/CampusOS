<script setup lang="ts">
/**
 * 修改密码（整合自 CampusOS_a 的 ChangePasswordView，独立页面版）。
 *
 * 顶栏用户下拉菜单「修改密码」直达此页；个人中心里的「修改密码」标签页仍然可用。
 * 对应后端 PUT /api/user/password；修改成功后清登录态，回登录页重新登录。
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { changePassword } from '@/api/user'
import { logout as logoutApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, value, cb) => (value === form.newPassword ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur',
    },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    // 修改成功后强制重新登录（与 CampusOS_a 行为一致）；登出接口失败也照常清本地态
    try { await logoutApi() } catch { /* 本地登出才是关键 */ }
    userStore.logout()
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="password-page">
    <el-card class="password-card" shadow="never">
      <h2 class="title">修改密码</h2>
      <p class="subtitle">为了账号安全，修改后需重新登录</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码（至少 6 位）" show-password />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleSubmit">
            确认修改
          </el-button>
        </el-form-item>

        <div class="back-link">
          <el-link type="primary" @click="router.push('/profile')">返回个人中心</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.password-page {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.password-card {
  width: 100%;
  max-width: 520px;
  padding: 14px 24px 6px;
}

.title {
  margin: 0 0 4px;
  text-align: center;
  color: #2c2350;
}

.subtitle {
  margin: 0 0 24px;
  text-align: center;
  font-size: 13px;
  color: #a89ec9;
}

.submit-btn {
  width: 100%;
}

.back-link {
  text-align: center;
  margin-bottom: 10px;
}
</style>
