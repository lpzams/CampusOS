<script setup lang="ts">
/**
 * 二手市场列表页（功能 12）。
 *
 * 筛选（分类/关键词/价格区间）+ 商品卡片网格 + 分页；「发布闲置」弹窗表单。
 * 对应后端 GET /api/product/list（分页参数 page/size）、POST /api/product。
 * 浏览公开，发布需登录（未登录点发布会被 request.ts 引导去登录页）。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { PRODUCT_CATEGORIES, createProduct, pageProducts } from '@/api/product'
import type { ProductForm, ProductItem } from '@/api/product'
import { formatMoney, summary } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const list = ref<ProductItem[]>([])
const total = ref(0)

const query = reactive({
  categoryId: undefined as number | undefined,
  keyword: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  page: 1,
  size: 12,
})

async function fetchList() {
  loading.value = true
  try {
    const page = await pageProducts(query)
    list.value = page.list
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function goDetail(id: number) {
  router.push(`/market/${id}`)
}

// ===== 发布闲置 =====
const publishVisible = ref(false)
const publishing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ProductForm>({
  title: '', price: 0, description: '', categoryId: 1, contactPhone: '', wechat: '',
})
const rules: FormRules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  description: [{ required: true, message: '请描述商品成色/使用情况', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

function openPublish() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再发布闲置')
    router.push({ path: '/login', query: { redirect: '/market' } })
    return
  }
  Object.assign(form, { title: '', price: 0, description: '', categoryId: 1, contactPhone: '', wechat: '' })
  publishVisible.value = true
}

async function handlePublish() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.price <= 0) {
    ElMessage.warning('价格必须大于 0')
    return
  }
  publishing.value = true
  try {
    await createProduct(form)
    ElMessage.success('发布成功')
    publishVisible.value = false
    handleSearch()
  } finally {
    publishing.value = false
  }
}

function categoryName(item: ProductItem) {
  return item.category || PRODUCT_CATEGORIES.find(c => c.id === item.categoryId)?.name || '其他'
}

onMounted(fetchList)
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="CAMPUS MARKET"
      title="让闲置在校园里继续流转"
      description="面向校内师生的轻量交易社区。教材、数码和生活用品，就近沟通，更快成交。"
      icon="↻"
      tone="orange"
      :metrics="[
        { label: '当前商品', value: total },
        { label: '商品分类', value: PRODUCT_CATEGORIES.length },
        { label: '安全提示', value: '当面验货' },
      ]"
    >
      <template #actions><el-button type="primary" plain @click="openPublish">发布闲置</el-button></template>
    </ServiceHero>
    <div class="category-strip">
      <button :class="{ active: query.categoryId === undefined }" @click="query.categoryId = undefined; handleSearch()">
        <span>全</span><strong>全部好物</strong>
      </button>
      <button v-for="(category, index) in PRODUCT_CATEGORIES" :key="category.id" :class="{ active: query.categoryId === category.id }" @click="query.categoryId = category.id; handleSearch()">
        <span>{{ ['数', '书', '用', '动', '趣'][index] }}</span><strong>{{ category.name }}</strong>
      </button>
    </div>
    <!-- 筛选栏 -->
    <el-card shadow="never" class="search-bar">
      <div class="search-form">
        <el-input
          v-model="query.keyword"
          placeholder="搜索商品…"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable class="category-select" @change="handleSearch">
          <el-option v-for="c in PRODUCT_CATEGORIES" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input-number v-model="query.minPrice" :min="0" placeholder="最低价" controls-position="right" class="price-input" />
        <span class="price-sep">-</span>
        <el-input-number v-model="query.maxPrice" :min="0" placeholder="最高价" controls-position="right" class="price-input" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="openPublish">发布闲置</el-button>
      </div>
    </el-card>

    <!-- 商品网格 -->
    <div v-loading="loading" class="product-grid-wrap">
      <el-empty v-if="!loading && list.length === 0" description="暂无商品，发布一件试试" />
      <div class="product-grid">
        <el-card v-for="item in list" :key="item.id" shadow="hover" class="product-card" @click="goDetail(item.id)">
          <div class="product-cover" :style="item.coverImage ? { backgroundImage: `url(${item.coverImage})` } : undefined">
            <span v-if="!item.coverImage">{{ categoryName(item).slice(0, 1) }}</span>
            <em>{{ categoryName(item) }}</em>
          </div>
          <div class="product-title-row">
            <span class="product-title">{{ item.title }}</span>
            <el-tag v-if="item.status === '已售'" size="small" type="info">已售</el-tag>
          </div>
          <p class="product-desc">{{ summary(item.description, 40) }}</p>
          <div class="product-bottom">
            <span class="product-price">{{ formatMoney(item.price) }}</span>
            <span class="product-meta">{{ categoryName(item) }} · {{ item.viewCount ?? 0 }} 次浏览</span>
          </div>
        </el-card>
      </div>
    </div>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="fetchList"
      />
    </div>

    <!-- 发布弹窗 -->
    <el-dialog v-model="publishVisible" title="发布闲置" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="60" placeholder="如：二手显示器" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="c in PRODUCT_CATEGORIES" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="成色、入手渠道、转手原因…" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" maxlength="11" />
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="form.wechat" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}

.category-strip { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-bottom: 16px; }
.category-strip button { display: flex; align-items: center; gap: 10px; padding: 12px; border: 1px solid #ece4fb; border-radius: 13px; background: #fff; color: #606266; cursor: pointer; transition: .2s ease; }
.category-strip button:hover, .category-strip button.active { color: #7c5cd6; border-color: #bfaeee; transform: translateY(-2px); box-shadow: 0 8px 20px #7c5cd619; }
.category-strip span { display: grid; place-items: center; width: 32px; height: 32px; flex: none; border-radius: 10px; color: #fff; background: linear-gradient(135deg, #ef9a52, #c35b45); }
.category-strip strong { font-size: 13px; white-space: nowrap; }

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 180px;
}

.category-select {
  width: 130px;
}

.price-input {
  width: 120px;
}

.price-sep {
  color: #909399;
}

.product-grid-wrap {
  min-height: 200px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 14px;
}

.product-card {
  cursor: pointer;
  overflow: hidden;
  transition: transform .2s ease;
}
.product-card:hover { transform: translateY(-4px); }
.product-card :deep(.el-card__body) { padding-top: 12px; }
.product-cover { position: relative; height: 112px; margin: -20px -20px 14px; display: grid; place-items: center; background: linear-gradient(135deg, #f4effd, #fae9df); background-position: center; background-size: cover; }
.product-cover > span { font-size: 38px; font-weight: 800; color: #7c5cd649; }
.product-cover em { position: absolute; left: 10px; bottom: 9px; padding: 3px 8px; color: #fff; font-size: 11px; font-style: normal; border-radius: 20px; background: #2f244dbb; }

.product-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.product-title {
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  margin: 8px 0;
  color: #909399;
  font-size: 13px;
  min-height: 36px;
}

.product-bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.product-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

.product-meta {
  color: #c0c4cc;
  font-size: 12px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
@media (max-width: 800px) { .category-strip { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 500px) { .category-strip { grid-template-columns: repeat(2, 1fr); } }
</style>
