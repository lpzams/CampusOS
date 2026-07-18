<template>
  <div class="location-list-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-bar">
      <div class="search-form">
        <el-input
          v-model="keyword"
          placeholder="搜索地点名称…"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="selectedCategory"
          placeholder="全部分类"
          clearable
          class="search-select"
          @change="handleSearch"
        >
          <el-option
            v-for="(label, code) in categoryMap"
            :key="code"
            :label="label"
            :value="code"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <!-- 地点列表 -->
    <div v-loading="loading" class="location-grid">
      <el-empty v-if="!loading && !list.length" description="暂无地点" />

      <div
        v-for="item in list"
        :key="item.id"
        class="location-card"
        @click="goToDetail(item.id)"
      >
        <div class="location-image">
          <el-image :src="item.image" fit="cover">
            <template #placeholder>
              <div class="image-placeholder">📍</div>
            </template>
            <template #error>
              <div class="image-placeholder">📍</div>
            </template>
          </el-image>
          <div class="location-category" :class="getCategoryClass(item.categoryCode)">
            {{ item.category }}
          </div>
        </div>
        <div class="location-info">
          <h3 class="location-name">{{ item.name }}</h3>
          <p class="location-address">{{ item.address || item.building }}</p>
          <p class="location-desc">{{ item.description || '暂无描述' }}</p>
          <div class="location-footer">
            <span class="location-coords">📍 {{ item.longitude }}, {{ item.latitude }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getLocationList, searchLocation } from '@/api/location'
import { LocationCategoryMap } from '@/api/types'
import type { LocationItem, LocationCategoryCode } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const list = ref<LocationItem[]>([])
const keyword = ref('')
const selectedCategory = ref<LocationCategoryCode | ''>('')

const categoryMap = LocationCategoryMap

function getCategoryClass(categoryCode: string) {
  const map: Record<string, string> = {
    BUILDING: 'cat-building',
    LIBRARY: 'cat-library',
    CANTEEN: 'cat-canteen',
    DORMITORY: 'cat-dormitory',
    GYM: 'cat-gym',
    OFFICE: 'cat-office',
  }
  return map[categoryCode] || 'cat-other'
}

async function fetchList() {
  loading.value = true
  try {
    // 如果有关键词，走搜索接口
    if (keyword.value.trim()) {
      const res = await searchLocation(keyword.value.trim())
      if (res.code === 200 && res.data) {
        // 搜索结果字段名略有不同，适配一下
        list.value = res.data.map((item: any) => ({
          id: item.id,
          name: item.name,
          category: item.category,
          categoryCode: item.categoryCode,
          longitude: item.longitude,
          latitude: item.latitude,
          address: item.address,
          building: item.building || '',
          image: '',
          description: '',
        }))
      }
      return
    }

    // 否则走列表接口
    const params: any = {}
    if (selectedCategory.value) params.category = selectedCategory.value
    const res = await getLocationList(params)
    if (res.code === 200 && res.data) {
      list.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchList()
}

function handleReset() {
  keyword.value = ''
  selectedCategory.value = ''
  fetchList()
}

function goToDetail(id: number) {
  router.push(`/location/${id}`)
}

onMounted(fetchList)
</script>

<style scoped>
.location-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
}

.search-select {
  width: 160px;
}

/* ===== 地点网格 ===== */
.location-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.location-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.location-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.location-image {
  position: relative;
  width: 100%;
  height: 160px;
  background: #f5f7fa;
  overflow: hidden;
}

.location-image .el-image {
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

.location-category {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #fff;
}

.cat-building { background: #409eff; }
.cat-library { background: #e6a23c; }
.cat-canteen { background: #67c23a; }
.cat-dormitory { background: #f56c6c; }
.cat-gym { background: #909399; }
.cat-office { background: #9b59b6; }
.cat-other { background: #95a5a6; }

.location-info {
  padding: 12px 14px 14px;
}

.location-name {
  margin: 0 0 4px 0;
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.location-address {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #909399;
}

.location-desc {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.location-footer {
  font-size: 13px;
  color: #909399;
}

@media (max-width: 640px) {
  .search-form {
    flex-direction: column;
  }
  .search-input,
  .search-select {
    width: 100% !important;
  }
  .location-grid {
    grid-template-columns: 1fr;
  }
}
</style>