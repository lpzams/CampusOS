<script setup lang="ts">
/**
 * 新闻管理页（后台）—— 表格 + 弹窗表单 + 行操作。
 *
 * 覆盖了三类写操作：
 *   发布：POST /admin/news（弹窗表单 + 前端校验）
 *   下线：PUT  /news/{id}/offline（二次确认）
 *   删除：DELETE /news/{id}（二次确认，后端是逻辑删除）
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getNewsList, getNewsCategories, publishNews, deleteNews, offlineNews } from '@/api/news'
import type { NewsListItem, NewsCategory, PublishNewsParams } from '@/api/types'
import { formatDateTime } from '@/utils/format'

// ============================================================
// ===== 列表 =====
// ============================================================

const loading = ref(false)
const list = ref<NewsListItem[]>([])
const categories = ref<NewsCategory[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

async function fetchCategories() {
  try {
    const res = await getNewsCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch {
    // 分类接口失败不影响主列表
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getNewsList({
      page: query.page,
      size: query.size,
    })
    if (res.code === 200 && res.data) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

// ============================================================
// ===== 发布新闻（弹窗表单） =====
// ============================================================

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<PublishNewsParams>({
  title: '',
  content: '',
  coverImage: '',
  categoryId: undefined as unknown as number,
  summary: '',
  isTop: false,
  isPublished: true,
})

const rules: FormRules = {
  title: [
    { required: true, message: '标题不能为空', trigger: 'blur' },
    { max: 200, message: '标题最长 200 字', trigger: 'blur' },
  ],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

function openCreateDialog() {
  form.title = ''
  form.content = ''
  form.coverImage = ''
  form.categoryId = undefined as unknown as number
  form.summary = ''
  form.isTop = false
  form.isPublished = true
  dialogVisible.value = true
}

// ===== 封面图处理 =====
function handleCoverChange(file: any) {
  const rawFile = file.raw
  if (!rawFile) return
  form.coverImage = URL.createObjectURL(rawFile)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await publishNews(form)
    if (res.code === 200) {
      ElMessage.success(`发布成功，新闻 id=${res.data?.id}`)
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } finally {
    submitting.value = false
  }
}

// ============================================================
// ===== 行操作：下线 / 删除 =====
// ============================================================

async function handleOffline(row: NewsListItem) {
  const ok = await ElMessageBox.confirm(
    `确定下线「${row.title}」吗？下线后前台不再展示。`,
    '下线确认',
    { type: 'warning' },
  ).catch(() => false)
  if (!ok) return

  await offlineNews(row.id)
  ElMessage.success('已下线')
  fetchList()
}

async function handleDelete(row: NewsListItem) {
  const ok = await ElMessageBox.confirm(
    `确定删除「${row.title}」吗？`,
    '删除确认',
    { type: 'error', confirmButtonText: '删除' },
  ).catch(() => false)
  if (!ok) return

  await deleteNews(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <span class="toolbar-title">新闻管理</span>
        <el-button type="primary" @click="openCreateDialog">发布新闻</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="栏目" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger" size="small">置顶</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="90" />
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="warning" @click="handleOffline(row as NewsListItem)">下线</el-button>
            <el-button link type="danger" @click="handleDelete(row as NewsListItem)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchList"
          @size-change="fetchList"
        />
      </div>
    </el-card>

    <!-- ============================================================ -->
    <!-- 发布新闻弹窗 -->
    <!-- ============================================================ -->
    <el-dialog v-model="dialogVisible" title="发布新闻" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入新闻标题" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="请输入新闻摘要（选填）"
          />
        </el-form-item>

        <el-form-item label="封面图">
          <div class="cover-upload">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleCoverChange"
              accept="image/jpeg,image/png,image/webp"
            >
              <el-button type="primary">选择图片</el-button>
            </el-upload>
            <div v-if="form.coverImage" class="cover-preview">
              <el-image :src="form.coverImage" fit="cover" style="width: 120px; height: 80px; border-radius: 4px;" />
              <el-button type="danger" text @click="form.coverImage = ''">移除</el-button>
            </div>
            <span class="tip">建议比例 16:9，不超过 2MB</span>
          </div>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入正文，支持 HTML 标签"
          />
        </el-form-item>

        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="立即发布">
          <el-switch v-model="form.isPublished" active-text="是" inactive-text="否" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar-title {
  font-size: 16px;
  font-weight: 600;
}
.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
.text-muted {
  color: #c0c4cc;
}
.cover-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.cover-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}
.tip {
  font-size: 12px;
  color: #909399;
}
</style>