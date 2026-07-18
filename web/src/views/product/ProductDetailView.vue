<template>
  <div v-loading="loading" class="product-detail-page">
    <div v-if="detail" class="detail-container">
      <!-- 返回 -->
      <el-page-header content="商品详情" @back="goBack" />

      <!-- 内容 -->
      <el-row :gutter="24" class="detail-content">
        <!-- 图片 -->
        <el-col :xs="24" :md="10">
          <div class="image-gallery">
            <el-image
              :src="detail.images?.[0] || defaultCover"
              fit="cover"
              style="width: 100%; height: 380px; border-radius: 10px;"
            >
              <template #placeholder>
                <div class="image-placeholder">📦</div>
              </template>
            </el-image>
            <div v-if="detail.images?.length > 1" class="image-thumbnails">
              <el-image
                v-for="(url, index) in detail.images"
                :key="index"
                :src="url"
                fit="cover"
                style="width: 70px; height: 60px; border-radius: 6px; cursor: pointer;"
                @click="currentImageIndex = index"
              />
            </div>
          </div>
        </el-col>

        <!-- 信息 -->
        <el-col :xs="24" :md="14">
          <div class="detail-info">
            <h1 class="detail-title">{{ detail.title }}</h1>

            <div class="price-area">
              <span class="price">¥{{ detail.price.toFixed(2) }}</span>
              <span v-if="detail.originalPrice" class="original-price">
                ¥{{ detail.originalPrice.toFixed(2) }}
              </span>
              <el-tag :type="detail.statusCode === 0 ? 'success' : 'danger'" size="small">
                {{ detail.status }}
              </el-tag>
            </div>

            <div class="meta-info">
              <span>📂 {{ detail.category }}</span>
              <span>👁️ {{ detail.viewCount }}</span>
              <span>❤️ {{ detail.favoriteCount }}</span>
              <span>📅 {{ formatDateTime(detail.createTime) }}</span>
            </div>

            <el-divider />

            <div class="seller-info">
              <div class="seller-header">
                <el-avatar :size="40" :src="detail.seller?.avatar" />
                <div class="seller-name">
                  <span>{{ detail.seller?.realName }}</span>
                  <el-tag size="small" type="info">卖家</el-tag>
                </div>
              </div>
              <div class="seller-contact">
                <span>📞 {{ detail.seller?.phone }}</span>
                <span v-if="detail.seller?.wechat">💬 {{ detail.seller?.wechat }}</span>
              </div>
            </div>

            <el-divider />

            <div class="description">
              <h4>商品描述</h4>
              <p>{{ detail.description }}</p>
            </div>

            <div class="action-bar">
              <el-button
                :type="detail.isFavorite ? 'danger' : 'primary'"
                :plain="!detail.isFavorite"
                @click="handleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ detail.isFavorite ? '已收藏' : '收藏' }}
              </el-button>

              <el-button
                v-if="detail.statusCode === 0"
                type="success"
                @click="openOrderDialog"
              >
                <el-icon><ShoppingCart /></el-icon>
                立即购买
              </el-button>

              <el-button
                v-else
                type="info"
                disabled
              >
                已售出
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading" description="商品不存在或已下架" />

    <!-- ===== 下单弹窗 ===== -->
    <el-dialog v-model="orderDialogVisible" title="确认购买" width="440px">
      <div class="order-confirm">
        <div class="order-product">
          <span>商品：{{ detail?.title }}</span>
          <span class="order-price">¥{{ detail?.price.toFixed(2) }}</span>
        </div>
        <el-form>
          <el-form-item label="留言">
            <el-input
              v-model="orderMessage"
              type="textarea"
              :rows="3"
              placeholder="给卖家留言（选填）"
              maxlength="200"
            />
          </el-form-item>
        </el-form>
        <div class="order-tip">
          ⚠️ 下单后请等待卖家确认，卖家将联系您完成交易
        </div>
      </div>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="orderLoading" @click="confirmOrder">
          确认下单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, ShoppingCart } from '@element-plus/icons-vue'
import { getProductDetail, favoriteProduct, createProductOrder } from '@/api/product'
import { useUserStore } from '@/stores/user'
import type { ProductDetail } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref<ProductDetail | null>(null)
const currentImageIndex = ref(0)
const defaultCover = 'https://via.placeholder.com/400x380/eee/999?text=No+Image'

// ===== 下单 =====
const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const orderMessage = ref('')

// ===== 获取详情 =====
async function fetchDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getProductDetail(String(id))
    if (res.code === 200 && res.data) {
      detail.value = res.data
    } else {
      ElMessage.warning(res.msg || '获取详情失败')
    }
  } finally {
    loading.value = false
  }
}

// ===== 收藏/取消收藏 =====
async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!detail.value) return

  try {
    const res = await favoriteProduct(detail.value.id)
    if (res.code === 200) {
      detail.value.isFavorite = !detail.value.isFavorite
      detail.value.favoriteCount += detail.value.isFavorite ? 1 : -1
      ElMessage.success(res.msg)
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// ===== 下单 =====
function openOrderDialog() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!detail.value || detail.value.statusCode === 1) {
    ElMessage.warning('商品已售出')
    return
  }
  orderMessage.value = ''
  orderDialogVisible.value = true
}

async function confirmOrder() {
  if (!detail.value) return

  orderLoading.value = true
  try {
    const res = await createProductOrder({
      productId: detail.value.id,
      message: orderMessage.value || '无留言',
    })
    if (res.code === 200 && res.data) {
      ElMessage.success('订单创建成功，请等待卖家确认')
      orderDialogVisible.value = false
      router.push('/my-orders')
    } else {
      ElMessage.error(res.msg || '下单失败')
    }
  } catch {
    ElMessage.error('下单失败，请重试')
  } finally {
    orderLoading.value = false
  }
}

function goBack() {
  router.push('/products')
}

onMounted(fetchDetail)
</script>

<style scoped>
.product-detail-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.detail-container {
  background: #fff;
  padding: 24px 32px 32px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.detail-content {
  margin-top: 16px;
}

.image-gallery {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-placeholder {
  width: 100%;
  height: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: #f5f7fa;
  border-radius: 10px;
}

.image-thumbnails {
  display: flex;
  gap: 8px;
}

.image-thumbnails .el-image {
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.image-thumbnails .el-image:hover {
  border-color: #409eff;
}

/* ===== 商品信息 ===== */
.detail-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #303133;
}

.price-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price {
  font-size: 30px;
  font-weight: 700;
  color: #f56c6c;
}

.original-price {
  font-size: 16px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.meta-info {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #909399;
}

.seller-info {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
}

.seller-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.seller-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.seller-contact {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.description h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #303133;
}

.description p {
  margin: 0;
  color: #606266;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* ===== 下单弹窗 ===== */
.order-product {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.order-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.order-tip {
  margin-top: 12px;
  padding: 10px 12px;
  background: #fdf6ec;
  border-radius: 6px;
  color: #e6a23c;
  font-size: 13px;
}

@media (max-width: 640px) {
  .detail-container {
    padding: 16px;
  }
}
</style>