<template>
  <div class="password-page">
    <el-card class="password-card">
      <h2 class="title">修改密码</h2>
      <p class="subtitle">修改后需重新登录</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" size="large">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit" style="width: 100%">
            确认修改
          </el-button>
        </el-form-item>

        <div class="back-link">
          <el-link type="primary" @click="goBack">返回个人中心</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (value !== form.newPassword) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const success = await userStore.changePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
      })
      if (success) {
        ElMessage.success('密码修改成功，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error('密码修改失败，请检查当前密码是否正确')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '修改异常')
    } finally {
      loading.value = false
    }
  })
}

function goBack() { router.push('/profile') }
</script>

<style scoped>
.password-page {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.password-card {
  width: 480px;
  padding: 30px 40px 20px;
  border-radius: 12px;
}
.title { text-align: center; margin-bottom: 4px; color: #333; }
.subtitle { text-align: center; font-size: 14px; color: #909399; margin-bottom: 24px; }
.back-link { text-align: center; margin-top: 8px; }
</style>