<template>
  <div class="product-list-page">
    <!-- 搜索栏 -->
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
        <el-select
          v-model="query.categoryId"
          placeholder="全部分类"
          clearable
          class="search-select"
          @change="handleSearch"
        >
          <el-option label="电子产品" :value="1" />
          <el-option label="学习资料" :value="2" />
          <el-option label="生活用品" :value="3" />
          <el-option label="其他" :value="4" />
        </el-select>
        <el-input-number
          v-model="query.minPrice"
          :min="0"
          placeholder="最低价"
          controls-position="right"
          style="width: 130px"
          @change="handleSearch"
        />
        <span class="price-sep">-</span>
        <el-input-number
          v-model="query.maxPrice"
          :min="0"
          placeholder="最高价"
          controls-position="right"
          style="width: 130px"
          @change="handleSearch"
        />
        <el-select
          v-model="query.status"
          placeholder="全部状态"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option label="在售" :value="0" />
          <el-option label="已售" :value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <div v-loading="loading" class="product-grid">
      <el-empty v-if="!loading && !list.length" description="暂无商品" />

      <div
        v-for="item in list"
        :key="item.id"
        class="product-card"
        @click="goToDetail(item.id)"
      >
        <div class="product-cover">
          <el-image :src="item.coverImage" fit="cover">
            <template #placeholder>
              <div class="image-placeholder">📦</div>
            </template>
            <template #error>
              <div class="image-placeholder">📦</div>
            </template>
          </el-image>
          <div v-if="item.statusCode === 1" class="sold-badge">已售</div>
        </div>
        <div class="product-info">
          <h3 class="product-title">{{ item.title }}</h3>
          <div class="product-meta">
            <span class="product-price">¥{{ item.price.toFixed(2) }}</span>
            <span class="product-original" v-if="item.originalPrice">
              ¥{{ item.originalPrice.toFixed(2) }}
            </span>
          </div>
          <div class="product-footer">
            <span class="product-category">{{ item.category }}</span>
            <span class="product-seller">{{ item.sellerName }}</span>
            <span class="product-views">👁️ {{ item.viewCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import type { ProductItem } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const list = ref<ProductItem[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 12,
  keyword: '',
  categoryId: undefined as number | undefined,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  status: undefined as 0 | 1 | undefined,
})

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
    }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.minPrice !== undefined) params.minPrice = query.minPrice
    if (query.maxPrice !== undefined) params.maxPrice = query.maxPrice
    if (query.status !== undefined) params.status = query.status

    const res = await getProductList(params)
    if (res.code === 200 && res.data) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function goToDetail(id: number) {
  router.push(`/product/${id}`)
}

onMounted(fetchList)
</script>

<style scoped>
.product-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
}

.search-select {
  width: 140px;
}

.price-sep {
  color: #909399;
}

/* ===== 商品网格 ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.product-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.product-cover {
  position: relative;
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  overflow: hidden;
}

.product-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: #f5f7fa;
}

.sold-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
}

.product-info {
  padding: 12px 14px 14px;
}

.product-title {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.product-original {
  font-size: 13px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}

.product-category {
  background: #f0f2f5;
  padding: 0 8px;
  border-radius: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 640px) {
  .search-form {
    flex-direction: column;
  }
  .search-input,
  .search-select {
    width: 100% !important;
  }
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
}
</style>