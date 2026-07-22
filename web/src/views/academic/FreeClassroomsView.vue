<script setup lang="ts">
/**
 * 空闲教室查询（整合自 CampusOS_a 的 FreeClassroomsView）。
 *
 * 对应后端 GET /api/course/free-classrooms?building=&date=&timeSlot=，
 * 返回教室扁平列表（name/building/capacity/timeSlot/available）。
 */
import { onMounted, reactive, ref } from 'vue'
import { getFreeClassrooms } from '@/api/course'
import type { Classroom } from '@/api/course'
import ServiceHero from '@/components/ServiceHero.vue'

const loading = ref(false)
const list = ref<Classroom[]>([])
const searched = ref(false)

const query = reactive({ building: '', date: '', timeSlot: '' })

const TIME_SLOTS = ['1-2节', '3-4节', '5-6节', '7-8节', '9-10节']

async function handleSearch() {
  loading.value = true
  try {
    list.value = await getFreeClassrooms({
      building: query.building || undefined,
      date: query.date || undefined,
      timeSlot: query.timeSlot || undefined,
    })
    searched.value = true
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.building = ''
  query.date = ''
  query.timeSlot = ''
  handleSearch()
}

onMounted(handleSearch)
</script>

<template>
  <div>
    <ServiceHero
      eyebrow="FREE CLASSROOMS"
      title="空闲教室查询"
      description="想找地方自习？按教学楼和时间段筛一筛，空闲教室一目了然。"
      icon="🏢"
      tone="green"
      :metrics="[{ label: '当前空闲', value: list.length, hint: '间教室' }]"
    />

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="教学楼">
          <el-input v-model="query.building" placeholder="如：教学楼A" clearable style="width: 160px" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 170px"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <el-select v-model="query.timeSlot" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option v-for="slot in TIME_SLOTS" :key="slot" :label="slot" :value="slot" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading">
      <el-empty v-if="!loading && searched && !list.length" description="该条件下暂无空闲教室" />

      <div v-else class="classroom-grid">
        <div v-for="room in list" :key="`${room.building}-${room.name}-${room.timeSlot}`" class="classroom-card">
          <div class="room-name">{{ room.name }}</div>
          <div class="room-info">{{ room.building || '-' }}</div>
          <div class="room-info">容量 {{ room.capacity ?? '-' }} 人</div>
          <div class="room-info">{{ room.timeSlot || '全天' }}</div>
          <el-tag type="success" size="small">空闲</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.classroom-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.classroom-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #ece4fb;
  border-radius: 14px;
  text-align: center;
  transition: transform 0.15s, box-shadow 0.15s;
}

.classroom-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 26px #7c5cd61f;
}

.room-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c2350;
}

.room-info {
  font-size: 13px;
  color: #a89ec9;
  margin: 2px 0;
}

@media (max-width: 640px) {
  .search-form :deep(.el-form-item) {
    margin-bottom: 10px;
  }
}
</style>
