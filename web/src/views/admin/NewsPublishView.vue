<template>
  <div class="publish-page">
    <el-card>
      <template #header>
        <h2 style="margin:0;">发布新闻</h2>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入新闻标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入新闻摘要（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 200px">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面图" prop="coverImage">
          <div class="cover-upload">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleCoverChange"
              :before-upload="beforeUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择封面图片
              </el-button>
            </el-upload>
            <div v-if="form.coverImage" class="cover-preview">
              <el-image :src="form.coverImage" fit="cover" style="width: 200px; height: 120px; border-radius: 6px;" />
              <el-button type="danger" text @click="form.coverImage = ''">移除</el-button>
            </div>
            <span class="tip">支持 JPG/PNG/WEBP，建议比例 16:9</span>
          </div>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入新闻内容（支持 HTML 标签）"
          />
        </el-form-item>

        <el-form-item label="置顶" prop="isTop">
          <el-switch v-model="form.isTop" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="立即发布" prop="isPublished">
          <el-switch v-model="form.isPublished" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发布
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { publishNews, getNewsCategories } from '@/api/news'
import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'
import type { NewsCategory } from '@/api/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const categories = ref<NewsCategory[]>([])

const form = reactive({
  title: '',
  summary: '',
  categoryId: undefined as number | undefined,
  coverImage: '',
  content: '',
  isTop: false,
  isPublished: true,
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入新闻标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度为 5-100 位', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入新闻内容', trigger: 'blur' }],
}

// ===== 获取分类 =====
async function fetchCategories() {
  try {
    const res = await getNewsCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch {
    // 静默失败
  }
}

// ===== 封面图上传 =====
const handleCoverChange = (uploadFile: any) => {
  const file = uploadFile.raw
  if (!file) return
  form.coverImage = URL.createObjectURL(file)
}

const beforeUpload = (rawFile: UploadRawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.warning('请上传图片文件'); return false }
  if (!isLt2M) { ElMessage.warning('图片大小不能超过 2MB'); return false }
  return false
}

// ===== 提交 =====
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!form.coverImage) {
      const confirm = await ElMessageBox.confirm('未上传封面图，是否继续发布？', '提示', {
        confirmButtonText: '继续发布',
        cancelButtonText: '取消',
        type: 'warning',
      }).catch(() => false)
      if (!confirm) return
    }

    submitting.value = true
    try {
      const res = await publishNews({
        title: form.title,
        content: form.content,
        coverImage: form.coverImage,
        categoryId: form.categoryId!,
        summary: form.summary,
        isTop: form.isTop,
        isPublished: form.isPublished,
      })
      if (res.code === 200) {
        ElMessage.success('新闻发布成功！')
        router.push('/news')
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

// ===== 重置 =====
function resetForm() {
  formRef.value?.resetFields()
  form.coverImage = ''
  form.isTop = false
  form.isPublished = true
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.publish-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tip {
  font-size: 12px;
  color: #909399;
}
</style>