<script setup lang="ts">
/**
 * 二手商品详情页（功能 12）。
 *
 * 对应后端 GET /api/product/detail/{id}。
 * 底部两个操作：收藏（POST /favorite/{id}）、留言/下单（POST /product/order）。
 * 两个都需要登录，未登录会被 request.ts 引导去登录页。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star } from '@element-plus/icons-vue'
import { PRODUCT_CATEGORIES, favoriteProduct, getProductDetail, orderProduct } from '@/api/product'
import type { ProductItem } from '@/api/product'
import { formatDateTime, formatMoney } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const product = ref<ProductItem | null>(null)

async function fetchDetail() {
  loading.value = true
  try {
    product.value = await getProductDetail(route.params.id as string)
  } finally {
    loading.value = false
  }
}

async function handleFavorite() {
  if (!product.value) return
  await favoriteProduct(product.value.id)
  ElMessage.success('已收藏')
}

// ===== 留言/下单弹窗 =====
const orderVisible = ref(false)
const ordering = ref(false)
const orderForm = reactive({ message: '' })

async function handleOrder() {
  if (!product.value) return
  ordering.value = true
  try {
    await orderProduct({ productId: product.value.id, message: orderForm.message })
    ElMessage.success('已发送，卖家会尽快联系你')
    orderVisible.value = false
    orderForm.message = ''
  } finally {
    ordering.value = false
  }
}

function categoryName(item: ProductItem) {
  return item.category || PRODUCT_CATEGORIES.find(c => c.id === item.categoryId)?.name || '其他'
}

onMounted(fetchDetail)
</script>

<template>
  <div v-loading="loading">
    <el-page-header content="商品详情" class="page-header" @back="router.back()" />

    <el-card v-if="product" shadow="never">
      <div class="detail-head">
        <h1 class="detail-title">{{ product.title }}</h1>
        <el-tag v-if="product.status === '已售'" type="info">已售出</el-tag>
      </div>
      <div class="detail-price">{{ formatMoney(product.price) }}</div>

      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="分类">{{ categoryName(product) }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ product.viewCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ product.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="微信号">{{ product.wechat || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDateTime(product.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">商品描述</el-divider>
      <div class="detail-desc">{{ product.description }}</div>

      <div class="detail-actions">
        <el-button @click="handleFavorite">
          <el-icon><Star /></el-icon>收藏
        </el-button>
        <el-button type="primary" :disabled="product.status === '已售'" @click="orderVisible = true">
          <el-icon><ChatDotRound /></el-icon>我想要
        </el-button>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="商品不存在或已下架" />

    <!-- 留言弹窗 -->
    <el-dialog v-model="orderVisible" title="联系卖家" width="420px">
      <el-input v-model="orderForm.message" type="textarea" :rows="4" placeholder="请问商品还在吗？可以约个时间当面看看…" />
      <template #footer>
        <el-button @click="orderVisible = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="handleOrder">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.detail-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.detail-price {
  margin: 12px 0 20px;
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.detail-info {
  margin-bottom: 8px;
}

.detail-desc {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
</style>
