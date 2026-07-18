<template>
  <div class="verify-page">
    <el-card class="verify-card">
      <h2 class="title">实名认证</h2>
      <p class="subtitle">提交真实身份信息，审核通过后即可使用全部功能</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" size="large">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>

        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学号" />
        </el-form-item>

        <el-form-item label="身份证正面" prop="idCardFront">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="(file: any) => handleFileChange(file, 'front')"
            :before-upload="beforeUpload"
            accept="image/jpeg,image/png"
          >
            <el-button type="primary"><el-icon><Upload /></el-icon>选择正面照片</el-button>
          </el-upload>
          <div v-if="form.idCardFront" class="preview">
            <el-image :src="form.idCardFront" fit="cover" style="width: 120px; height: 80px; border-radius: 4px;" />
          </div>
        </el-form-item>

        <el-form-item label="身份证反面" prop="idCardBack">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="(file: any) => handleFileChange(file, 'back')"
            :before-upload="beforeUpload"
            accept="image/jpeg,image/png"
          >
            <el-button type="primary"><el-icon><Upload /></el-icon>选择反面照片</el-button>
          </el-upload>
          <div v-if="form.idCardBack" class="preview">
            <el-image :src="form.idCardBack" fit="cover" style="width: 120px; height: 80px; border-radius: 4px;" />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit" style="width: 100%">
            提交认证
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { submitVerifyApi } from '@/api/auth'
import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  realName: '',
  idCard: '',
  studentId: '',
  idCardFront: '',
  idCardBack: '',
})

const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 位', trigger: 'blur' },
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' },
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 6, max: 20, message: '学号长度为 6-20 位', trigger: 'blur' },
  ],
  idCardFront: [{ required: true, message: '请上传身份证正面照片', trigger: 'change' }],
  idCardBack: [{ required: true, message: '请上传身份证反面照片', trigger: 'change' }],
}

const handleFileChange = (uploadFile: any, type: 'front' | 'back') => {
  const file = uploadFile.raw
  if (!file) return
  const url = URL.createObjectURL(file)
  if (type === 'front') form.idCardFront = url
  else form.idCardBack = url
}

const beforeUpload = (rawFile: UploadRawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.warning('请上传图片文件'); return false }
  if (!isLt2M) { ElMessage.warning('图片大小不能超过 2MB'); return false }
  return false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await submitVerifyApi({
        realName: form.realName,
        idCard: form.idCard,
        idCardFront: form.idCardFront,
        idCardBack: form.idCardBack,
        studentId: form.studentId,
      })
      if (res.code === 200 && res.data) {
        ElMessage.success('实名认证提交成功，等待审核')
        router.push('/profile')
      } else {
        ElMessage.error(res.msg || '提交失败')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '提交异常')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.verify-page {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.verify-card {
  width: 560px;
  padding: 30px 40px 20px;
  border-radius: 12px;
}
.title { text-align: center; margin-bottom: 4px; color: #333; }
.subtitle { text-align: center; font-size: 14px; color: #909399; margin-bottom: 24px; }
.preview { margin-top: 8px; }
</style>