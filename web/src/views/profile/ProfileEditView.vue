<template>
  <div class="profile-edit-page">
    <el-card class="profile-edit-card">
      <div class="page-header">
        <h2>编辑个人信息</h2>
        <el-button type="text" @click="goBack">返回</el-button>
      </div>

      <el-divider />

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
            <el-radio label="保密">保密</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            保存修改
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  realName: '',
  phone: '',
  email: '',
  gender: '保密' as '男' | '女' | '保密',
})

const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 位', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
}

// ===== 加载当前个人信息 =====
async function loadProfile() {
  if (!userStore.profile) {
    await userStore.fetchProfile()
  }
  if (userStore.profile) {
    form.realName = userStore.profile.realName
    form.phone = userStore.profile.phone
    form.email = userStore.profile.email
    form.gender = userStore.profile.gender
  }
}

// ===== 提交修改 =====
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const result = await userStore.updateProfile(form)
      if (result) {
        ElMessage.success('个人信息修改成功')
        router.push('/profile')
      } else {
        ElMessage.error('修改失败，请重试')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '修改异常')
    } finally {
      loading.value = false
    }
  })
}

// ===== 返回个人中心 =====
function goBack() {
  router.push('/profile')
}

// ===== 页面加载 =====
onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-edit-page {
  padding: 20px;
}
.profile-edit-card {
  max-width: 600px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
}
</style>