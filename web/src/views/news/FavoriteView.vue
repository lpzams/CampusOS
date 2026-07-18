<template>
  <div class="favorite-page">
    <h2 class="page-title">我的收藏</h2>

    <div v-loading="loading" class="favorite-list">
      <div
        v-for="item in favoriteList"
        :key="item.id"
        class="favorite-item"
        @click="goToDetail(item.id)"
      >
        <div class="item-cover">
          <el-image :src="item.coverImage" fit="cover">
            <template #placeholder>
              <div class="image-placeholder">📰</div>
            </template>
            <template #error>
              <div class="image-placeholder">📰</div>
            </template>
          </el-image>
        </div>
        <div class="item-info">
          <h4 class="item-title">{{ item.title }}</h4>
          <p class="item-summary">{{ item.summary || '暂无摘要' }}</p>
          <div class="item-meta">
            <span>📂 {{ item.category }}</span>
            <span>👁️ {{ item.viewCount }}</span>
            <span>❤️ {{ formatDateTime(item.createTime) }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && favoriteList.length === 0" description="暂无收藏" />
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:page-size="pageSize"
        v-model:current-page="currentPage"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchFavorites"
        @current-change="fetchFavorites"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavoriteList } from '@/api/news'
import type { FavoriteNewsItem } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const favoriteList = ref<FavoriteNewsItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getFavoriteList({
      page: currentPage.value,
      size: pageSize.value,
    })
    if (res.code === 200 && res.data) {
      favoriteList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

function goToDetail(id: number) {
  router.push(`/news/${id}`)
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.favorite-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 24px 0;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 300px;
}

.favorite-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.favorite-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.item-cover {
  flex-shrink: 0;
  width: 150px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
}

.item-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  background: #f0f2f5;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.item-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
}

.item-summary {
  margin: 0 0 6px 0;
  color: #666;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 16px;
  color: #999;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 640px) {
  .favorite-item {
    flex-direction: column;
  }
  .item-cover {
    width: 100%;
    height: 140px;
  }
}
</style>