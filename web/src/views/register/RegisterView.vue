<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="title">用户注册</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio :label="1">学生</el-radio>
            <el-radio :label="2">教师</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="学号/工号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入学号（学生）或工号（教师）"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="院系" prop="department">
          <el-input v-model="form.department" placeholder="请输入院系名称" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            注 册
          </el-button>
        </el-form-item>

        <div class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="goToLogin">立即登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi } from '../../api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  userType: 1 as 1 | 2, // 默认选学生
  username: '',
  realName: '',
  department: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
})

// 校验确认密码是否一致
const validateConfirmPassword = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  username: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { min: 6, max: 20, message: '学号/工号长度为 6-20 位', trigger: 'blur' },
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 位', trigger: 'blur' },
  ],
  department: [
    { required: true, message: '请输入院系名称', trigger: 'blur' },
    { min: 2, max: 50, message: '院系名称长度为 2-50 位', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      // 构造注册参数（去掉 confirmPassword）
      const { confirmPassword, ...registerData } = form
      const res = await registerApi({
        ...registerData,
        studentId: form.username,
      })
      if (res.code === 200) {
        ElMessage.success('注册成功！请登录')
        // 注册成功跳转到登录页
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '注册失败')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '注册异常，请检查后端服务')
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
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 520px;
  padding: 30px 40px 20px;
  border-radius: 12px;
}
.title {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
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