<script setup lang="ts">
/**
 * 新闻管理页（后台）—— 「写」页面的标准写法示例：表格 + 弹窗表单 + 行操作。
 *
 * 覆盖了三类写操作，分别对应后端 NewsController 的三个端点：
 *   发布：POST /api/news（弹窗表单 + 前端校验）
 *   下线：PUT  /api/news/{id}/offline（二次确认）
 *   删除：DELETE /api/news/{id}（二次确认，后端是逻辑删除）
 *
 * 【简化说明】列表复用了"已发布"查询接口，所以下线后条目会从表格里消失。
 * 真实系统会给后台单独做一个"查全部状态"的接口，等做权限时再加。
 *
 * 【新增功能时】做管理页照抄本文件：换 api 函数、表格列、表单字段与校验规则。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { NEWS_CATEGORIES, createNews, deleteNews, offlineNews, pageNews } from '@/api/news'
import type { CreateNewsForm, NewsItem } from '@/api/news'
import { formatDateTime } from '@/utils/format'

// ===== 列表 =====
const loading = ref(false)
const list = ref<NewsItem[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

async function fetchList() {
  loading.value = true
  try {
    const page = await pageNews(query)
    list.value = page.list
    total.value = page.total
  } finally {
    loading.value = false
  }
}

// ===== 发布新闻（弹窗表单） =====
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<CreateNewsForm>({
  title: '',
  content: '',
  author: '',
  category: '',
})

// 前端校验规则与后端 CreateNewsCommand 的注解保持一致。
// 注意：前端校验只是为了体验（即时提示），后端 @Valid 才是最终防线。
const rules: FormRules = {
  title: [
    { required: true, message: '标题不能为空', trigger: 'blur' },
    { max: 200, message: '标题最长 200 字', trigger: 'blur' },
  ],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  author: [{ required: true, message: '作者不能为空', trigger: 'blur' }],
  category: [{ required: true, message: '请选择栏目', trigger: 'change' }],
}

function openCreateDialog() {
  // 每次打开都重置表单，避免残留上一次的输入
  form.title = ''
  form.content = ''
  form.author = ''
  form.category = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  // validate() 不通过会 reject，直接 return 即可（错误提示由表单自己显示）
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const id = await createNews(form)
    ElMessage.success(`发布成功，新闻 id=${id}`)
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ===== 行操作：下线 / 删除（都要二次确认，防误点） =====
async function handleOffline(row: NewsItem) {
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

async function handleDelete(row: NewsItem) {
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

onMounted(fetchList)
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
        <el-table-column prop="author" label="作者" width="110" />
        <el-table-column prop="viewCount" label="浏览量" width="90" />
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.publishedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <!-- el-table 插槽的 row 类型是宽泛的 DefaultRow，这里断言回 NewsItem -->
            <el-button link type="warning" @click="handleOffline(row as NewsItem)">下线</el-button>
            <el-button link type="danger" @click="handleDelete(row as NewsItem)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 发布新闻弹窗 -->
    <el-dialog v-model="dialogVisible" title="发布新闻" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入新闻标题" />
        </el-form-item>
        <el-form-item label="栏目" prop="category">
          <el-select v-model="form.category" placeholder="请选择栏目" style="width: 100%">
            <el-option v-for="c in NEWS_CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="如：教务处" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入正文，支持换行" />
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
</style>
