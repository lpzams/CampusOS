<script setup lang="ts">
/**
 * 注册页（功能 1）。
 *
 * 对应后端 POST /api/auth/register：注册成功即自动登录（返回结构同登录）。
 * 只开放学生/教师注册（userType 1/2），管理员由后台种子数据创建。
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/auth'
import type { RegisterForm } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const submitting = ref(false)
const formRef = ref<FormInstance>()

// confirmPassword 只在前端用来做二次确认，不提交给后端
const form = reactive<RegisterForm & { confirmPassword: string }>({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  userType: 1,
  department: '',
  studentId: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入学号/工号（登录用）', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_r, value, cb) => (value === form.password ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur',
    },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  userType: [{ required: true, message: '请选择身份', trigger: 'change' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    // 学号/工号与登录名一致，学生的 studentId 默认等于 username
    const { confirmPassword, ...payload } = form
    void confirmPassword
    if (!payload.studentId) payload.studentId = payload.username
    const result = await register(payload)
    userStore.login(result)
    ElMessage.success('注册成功，已自动登录')
    router.replace('/news')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <el-card class="register-card" shadow="always">
      <router-link to="/" class="brand">CampusOS</router-link>
      <p class="slogan">创建校园门户账号</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="身份" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio :value="1">学生</el-radio>
            <el-radio :value="2">教师</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.userType === 1 ? '学号' : '工号'" prop="username">
          <el-input v-model="form.username" :placeholder="form.userType === 1 ? '如 2021001' : '如 T2021'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="用于验证码登录与找回密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="form.department" placeholder="选填，如 计算机学院" />
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">注 册</el-button>
      </el-form>

      <div class="extra-row">
        <span>已有账号？</span>
        <el-button link type="primary" @click="router.push('/login')">去登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #241457 0%, #7c5cd6 55%, #b06fd0 100%);
}

.register-card {
  width: 440px;
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

.extra-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 12px;
  color: #606266;
  font-size: 13px;
}
</style>
