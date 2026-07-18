<template>
  <el-dialog
    v-model="visible"
    title="更换头像"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="upload-container">
      <!-- 当前头像预览 -->
      <div class="preview-section">
        <div class="preview-label">当前头像</div>
        <el-avatar :size="120" :src="currentAvatar || defaultAvatar">
          {{ userStore.name?.[0] }}
        </el-avatar>
      </div>

      <el-divider>
        <el-icon><ArrowRight /></el-icon>
      </el-divider>

      <!-- 新头像预览 -->
      <div class="preview-section">
        <div class="preview-label">新头像预览</div>
        <el-avatar :size="120" :src="previewUrl || defaultAvatar">
          {{ userStore.name?.[0] }}
        </el-avatar>
      </div>
    </div>

    <!-- 上传按钮 -->
    <div class="upload-actions">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept="image/jpeg,image/png,image/gif,image/webp"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          选择图片
        </el-button>
      </el-upload>
      <span class="upload-tip">支持 JPG/PNG/GIF/WEBP，大小不超过 2MB</span>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!selectedFile"
        @click="handleConfirm"
      >
        确认上传
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { UploadInstance, UploadFile, UploadRawFile } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success', avatar: string): void
}>()

const userStore = useUserStore()
const uploadRef = ref<UploadInstance>()
const loading = ref(false)
const selectedFile = ref<File | null>(null)
const previewUrl = ref<string>('')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const currentAvatar = computed(() => userStore.avatar)

// ===== 文件选择 =====
const handleFileChange = (uploadFile: UploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  selectedFile.value = file
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = URL.createObjectURL(file)
}

// ===== 文件上传前校验 =====
const beforeUpload = (rawFile: UploadRawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.warning('请上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.warning('图片大小不能超过 2MB')
    return false
  }
  return false
}

// ===== 确认上传 =====
const handleConfirm = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  loading.value = true
  try {
    const newAvatar = await userStore.updateAvatar(selectedFile.value)
    if (newAvatar) {
      ElMessage.success('头像更新成功')
      emit('success', newAvatar)
      handleClose()
    } else {
      ElMessage.error('头像上传失败，请重试')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '上传异常')
  } finally {
    loading.value = false
  }
}

// ===== 关闭弹窗 =====
const handleClose = () => {
  selectedFile.value = null
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  visible.value = false
}
</script>

<style scoped>
.upload-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  padding: 20px 0;
}
.preview-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.preview-label {
  font-size: 14px;
  color: #909399;
}
.upload-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
}
:deep(.el-upload) {
  width: 100%;
}
</style>