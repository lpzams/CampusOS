<script setup lang="ts">
/**
 * 校园活动列表页（功能 13）。
 *
 * 筛选（分类/状态）+ 活动卡片网格（含报名进度条）+ 分页。
 * 对应后端 GET /api/activity/list（分页参数 page/size）。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, Location } from '@element-plus/icons-vue'
import { ACTIVITY_CATEGORIES, pageActivities } from '@/api/activity'
import type { ActivityItem } from '@/api/activity'
import { formatDateTime } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const router = useRouter()

const loading = ref(false)
const list = ref<ActivityItem[]>([])
const total = ref(0)

const ACTIVITY_STATUS = [
  { code: 'UPCOMING', name: '即将开始' },
  { code: 'ONGOING', name: '进行中' },
  { code: 'ENDED', name: '已结束' },
]

const query = reactive({ category: '', status: '', page: 1, size: 12 })

async function fetchList() {
  loading.value = true
  try {
    const page = await pageActivities(query)
    list.value = page.list
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  query.page = 1
  fetchList()
}

function goDetail(id: number) {
  router.push(`/activity/${id}`)
}

/** 报名进度百分比 */
function progress(item: ActivityItem): number {
  const max = Number(item.maxParticipants) || 0
  const cur = Number(item.currentParticipants) || 0
  if (max <= 0) return 0
  return Math.min(100, Math.round((cur / max) * 100))
}

onMounted(fetchList)
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="CAMPUS ACTIVITIES"
      title="把课表之外的时间交给热爱"
      description="发现体育、文艺、学术和志愿活动，找到同频伙伴，报名后在活动详情里完成签到。"
      icon="✦"
      tone="violet"
      :metrics="[
        { label: '活动总数', value: total },
        { label: '活动分类', value: ACTIVITY_CATEGORIES.length },
        { label: '当前筛选', value: query.status ? (ACTIVITY_STATUS.find(item => item.code === query.status)?.name || '') : '全部' },
      ]"
    />
    <div class="activity-categories">
      <button :class="{ active: !query.category }" @click="query.category = ''; handleFilter()">全部活动</button>
      <button v-for="category in ACTIVITY_CATEGORIES" :key="category.code" :class="{ active: query.category === category.code }" @click="query.category = category.code; handleFilter()">{{ category.name }}</button>
    </div>
    <el-card shadow="never" class="search-bar">
      <div class="search-form">
        <el-select v-model="query.category" placeholder="全部分类" clearable class="filter-select" @change="handleFilter">
          <el-option v-for="c in ACTIVITY_CATEGORIES" :key="c.code" :label="c.name" :value="c.code" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable class="filter-select" @change="handleFilter">
          <el-option v-for="s in ACTIVITY_STATUS" :key="s.code" :label="s.name" :value="s.code" />
        </el-select>
      </div>
    </el-card>

    <div v-loading="loading" class="activity-grid-wrap">
      <el-empty v-if="!loading && list.length === 0" description="暂无活动" />
      <div class="activity-grid">
        <el-card v-for="item in list" :key="item.id" shadow="hover" class="activity-card" @click="goDetail(item.id)">
          <div class="activity-cover" :style="item.coverImage ? { backgroundImage: `url(${item.coverImage})` } : undefined">
            <span>{{ (item.category || item.categoryCode || '活动').slice(0, 1) }}</span>
            <el-tag size="small" effect="dark">{{ item.status || '报名中' }}</el-tag>
          </div>
          <div class="activity-head">
            <span class="activity-title">{{ item.title }}</span>
            <el-tag size="small">{{ item.category || item.categoryCode }}</el-tag>
          </div>
          <div class="activity-info">
            <div class="info-row"><el-icon><Clock /></el-icon><span>{{ formatDateTime(item.startTime) }}</span></div>
            <div class="info-row"><el-icon><Location /></el-icon><span>{{ item.location }}</span></div>
          </div>
          <div class="activity-progress">
            <div class="progress-text">
              <span>{{ item.currentParticipants ?? 0 }}/{{ item.maxParticipants ?? 0 }} 人</span>
              <span>{{ item.status }}</span>
            </div>
            <el-progress :percentage="progress(item)" :show-text="false" :stroke-width="6" color="#7c5cd6" />
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
  </div>
</template>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}

.activity-categories { display: flex; gap: 8px; margin-bottom: 14px; overflow-x: auto; }
.activity-categories button { flex: none; padding: 8px 16px; border: 1px solid #e5def7; border-radius: 20px; color: #706780; background: #fff; cursor: pointer; }
.activity-categories button.active, .activity-categories button:hover { color: #fff; border-color: #7c5cd6; background: #7c5cd6; }

.search-form {
  display: flex;
  gap: 12px;
}

.filter-select {
  width: 160px;
}

.activity-grid-wrap {
  min-height: 200px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.activity-card {
  cursor: pointer;
  overflow: hidden;
  transition: transform .2s ease, box-shadow .2s ease;
}
.activity-card:hover { transform: translateY(-4px); }
.activity-card :deep(.el-card__body) { padding-top: 12px; }
.activity-cover { position: relative; height: 126px; margin: -20px -20px 14px; display: grid; place-items: center; background: linear-gradient(135deg, #e9e2fa, #dff2ed); background-position: center; background-size: cover; }
.activity-cover::after { content: ''; position: absolute; inset: 0; background: linear-gradient(180deg, transparent 45%, #25163f66); }
.activity-cover > span { position: relative; z-index: 1; font-size: 46px; font-weight: 800; color: #fff; text-shadow: 0 4px 15px #25163f55; }
.activity-cover :deep(.el-tag) { position: absolute; z-index: 2; right: 10px; top: 10px; border: 0; }

.activity-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
  margin-bottom: 4px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
