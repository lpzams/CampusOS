<template>
  <div class="submit-repair-page">
    <el-card>
      <template #header>
        <span class="title">🔧 提交报修</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        style="max-width: 700px; margin: 0 auto;"
      >
        <el-form-item label="报修类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择报修类型" style="width: 100%">
            <el-option
              v-for="(label, value) in repairTypeMap"
              :key="value"
              :label="label"
              :value="label"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请简要描述问题" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述问题情况"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片上传">
          <div class="image-upload">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
              :before-upload="beforeUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择图片
              </el-button>
            </el-upload>
            <span class="tip">支持 JPG/PNG/WEBP，建议上传 1-3 张</span>
          </div>
          <div v-if="imageList.length" class="image-preview-list">
            <div
              v-for="(url, index) in imageList"
              :key="index"
              class="image-preview-item"
            >
              <el-image :src="url" fit="cover" style="width: 80px; height: 80px; border-radius: 6px;" />
              <el-button
                type="danger"
                text
                circle
                class="remove-btn"
                @click="removeImage(index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="宿舍楼" prop="building">
          <el-input v-model="form.building" placeholder="如：6栋" />
        </el-form-item>

        <el-form-item label="房间号" prop="room">
          <el-input v-model="form.room" placeholder="如：302室" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" style="width: 100%">
            提交报修
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
import { Upload, Close } from '@element-plus/icons-vue'
import { submitRepair } from '@/api/repair'
import { RepairTypeMap } from '@/api/types'
import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const imageList = ref<string[]>([])

const repairTypeMap = RepairTypeMap

const form = reactive({
  type: '',
  title: '',
  description: '',
  images: [] as string[],
  building: '',
  room: '',
  contactPhone: '',
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度为 2-50 位', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 5, max: 500, message: '描述长度为 5-500 位', trigger: 'blur' },
  ],
  building: [
    { required: true, message: '请输入宿舍楼', trigger: 'blur' },
  ],
  room: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

// ===== 图片上传 =====
const handleImageChange = (uploadFile: any) => {
  const file = uploadFile.raw
  if (!file) return
  const url = URL.createObjectURL(file)
  if (imageList.value.length < 5) {
    imageList.value.push(url)
    form.images.push(url)
  } else {
    ElMessage.warning('最多上传 5 张图片')
  }
}

const beforeUpload = (rawFile: UploadRawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.warning('请上传图片文件'); return false }
  if (!isLt2M) { ElMessage.warning('图片大小不能超过 2MB'); return false }
  return false
}

const removeImage = (index: number) => {
  imageList.value.splice(index, 1)
  form.images.splice(index, 1)
}

// ===== 提交 =====
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const res = await submitRepair({
        type: form.type,
        title: form.title,
        description: form.description,
        images: form.images,
        building: form.building,
        room: form.room,
        contactPhone: form.contactPhone,
      })
      if (res.code === 200 && res.data) {
        ElMessage.success(`报修提交成功！预计处理时间：${res.data.expectedTime}`)
        router.push('/repair')
      } else {
        ElMessage.error(res.msg || '提交失败')
      }
    } catch {
      ElMessage.error('提交失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.submit-repair-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.image-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tip {
  font-size: 12px;
  color: #909399;
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.image-preview-item {
  position: relative;
}

.remove-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  padding: 0;
  width: 20px;
  height: 20px;
  font-size: 12px;
}
</style>