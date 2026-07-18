<template>
  <div v-loading="loading" class="location-detail-page">
    <div v-if="detail" class="detail-container">
      <!-- 返回 -->
      <el-page-header content="地点详情" @back="goBack" />

      <!-- 封面 -->
      <div v-if="detail.image" class="cover-wrapper">
        <el-image :src="detail.image" fit="cover" style="width: 100%; max-height: 300px;" />
      </div>

      <!-- 标题与分类 -->
      <div class="detail-header">
        <h1 class="detail-title">{{ detail.name }}</h1>
        <el-tag :type="getCategoryTagType(detail.categoryCode)" size="default">
          {{ detail.category }}
        </el-tag>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="地址">
          {{ detail.address || detail.building }}
        </el-descriptions-item>
        <el-descriptions-item label="坐标">
          {{ detail.longitude }}, {{ detail.latitude }}
        </el-descriptions-item>
        <el-descriptions-item label="开放时间">
          {{ detail.openTime || '全天' }}
        </el-descriptions-item>
        <el-descriptions-item label="设施">
          <span v-if="detail.facilities?.length">
            {{ detail.facilities.join('、') }}
          </span>
          <span v-else>暂无</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 描述 -->
      <el-divider content-position="left">介绍</el-divider>
      <div class="description">{{ detail.description }}</div>

      <!-- 楼层房间 -->
      <div v-if="detail.floors?.length">
        <el-divider content-position="left">楼层分布</el-divider>
        <div class="floors">
          <div
            v-for="floor in detail.floors"
            :key="floor.floor"
            class="floor-item"
          >
            <div class="floor-number">{{ floor.floor }}F</div>
            <div class="floor-rooms">
              <el-tag
                v-for="room in floor.rooms"
                :key="room"
                size="small"
                type="info"
                style="margin: 2px 4px;"
              >
                {{ room }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 路径规划按钮 -->
      <div class="action-bar">
        <el-button
          type="primary"
          size="large"
          @click="goToRoute"
        >
          <el-icon><Location /></el-icon>
          导航到这里
        </el-button>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="地点不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getLocationDetail } from '@/api/location'
import type { LocationDetail } from '@/api/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<LocationDetail | null>(null)

function getCategoryTagType(categoryCode: string) {
  const map: Record<string, 'danger' | 'warning' | 'primary' | 'success' | 'info'> = {
    BUILDING: 'primary',
    LIBRARY: 'warning',
    CANTEEN: 'success',
    DORMITORY: 'danger',
    GYM: 'info',
    OFFICE: 'info',
  }
  return map[categoryCode] || 'info'
}

async function fetchDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getLocationDetail(String(id))
    if (res.code === 200 && res.data) {
      detail.value = res.data
    } else {
      ElMessage.warning(res.msg || '获取详情失败')
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/locations')
}

function goToRoute() {
  if (!detail.value) return
  // 跳转到路径规划页，传递目标地点坐标
  router.push({
    path: '/route-planner',
    query: {
      toLng: String(detail.value.longitude),
      toLat: String(detail.value.latitude),
      toName: detail.value.name,
    },
  })
}

onMounted(fetchDetail)
</script>

<style scoped>
.location-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.detail-container {
  background: #fff;
  padding: 24px 32px 32px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.cover-wrapper {
  margin: 16px 0 20px;
  border-radius: 8px;
  overflow: hidden;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.detail-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  color: #303133;
}

.description {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.floors {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.floor-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.floor-number {
  font-weight: 600;
  color: #303133;
  width: 50px;
  flex-shrink: 0;
}

.floor-rooms {
  display: flex;
  flex-wrap: wrap;
}

.action-bar {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

@media (max-width: 640px) {
  .detail-container {
    padding: 16px;
  }
  .detail-title {
    font-size: 20px;
  }
}
</style>