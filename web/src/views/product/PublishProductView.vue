<template>
  <div class="publish-product-page">
    <el-card>
      <template #header>
        <span class="title">📦 发布商品</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        style="max-width: 700px; margin: 0 auto;"
      >
        <el-form-item label="商品标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入商品标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :step="10"
            placeholder="请输入价格"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option label="电子产品" :value="1" />
            <el-option label="学习资料" :value="2" />
            <el-option label="生活用品" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述商品情况（成色、使用时间、有无损坏等）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品图片">
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
            <span class="tip">支持 JPG/PNG/WEBP，建议上传 2-5 张</span>
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

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="微信号" prop="wechat">
          <el-input v-model="form.wechat" placeholder="请输入微信号（选填）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" style="width: 100%">
            发布商品
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
import { publishProduct } from '@/api/product'
import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const imageList = ref<string[]>([])

const form = reactive({
  title: '',
  price: undefined as number | undefined,
  description: '',
  categoryId: undefined as number | undefined,
  images: [] as string[],
  contactPhone: '',
  wechat: '',
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 3, max: 50, message: '标题长度为 3-50 位', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于 0', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 5, max: 500, message: '描述长度为 5-500 位', trigger: 'blur' },
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
    if (form.images.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }

    submitting.value = true
    try {
      const res = await publishProduct({
        title: form.title,
        price: form.price!,
        description: form.description,
        categoryId: form.categoryId!,
        images: form.images,
        contactPhone: form.contactPhone,
        wechat: form.wechat || '',
      })
      if (res.code === 200 && res.data) {
        ElMessage.success(`发布成功！审核中，预计 ${res.data.expectedAuditTime} 完成审核`)
        router.push('/products')
      } else {
        ElMessage.error(res.msg || '发布失败')
      }
    } catch {
      ElMessage.error('发布失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.publish-product-page {
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