<template>
  <div class="free-classrooms-page">
    <el-card>
      <template #header>
        <span class="title">🏢 空闲教室查询</span>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="教学楼">
          <el-input v-model="query.building" placeholder="如：教学楼A" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <el-select v-model="query.timeSlot" placeholder="全部" clearable style="width: 140px">
            <el-option label="1-2节 (08:00-09:35)" value="1-2节" />
            <el-option label="3-4节 (10:00-11:35)" value="3-4节" />
            <el-option label="5-6节 (14:00-15:35)" value="5-6节" />
            <el-option label="7-8节 (16:00-17:35)" value="7-8节" />
            <el-option label="9-10节 (19:00-20:35)" value="9-10节" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 结果 -->
      <div v-loading="loading">
        <div v-if="result" class="result-info">
          <span>{{ result.date }} {{ result.timeSlot }} | {{ result.building }}</span>
          <span class="count">共 {{ result.freeClassrooms?.length || 0 }} 间空闲教室</span>
        </div>

        <el-empty v-if="!loading && !result?.freeClassrooms?.length" description="暂无空闲教室" />

        <div v-else class="classroom-grid">
          <div
            v-for="room in result?.freeClassrooms || []"
            :key="room.classroom"
            class="classroom-card"
          >
            <div class="room-name">{{ room.classroom }}</div>
            <div class="room-info">{{ room.building }} {{ room.floor }}F</div>
            <div class="room-info">容量 {{ room.capacity }} 人</div>
            <div class="room-info">{{ room.type }}</div>
            <el-tag type="success" size="small">空闲</el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getFreeClassrooms } from '@/api/course'
import type { FreeClassroomsResponse } from '@/api/types'

const loading = ref(false)
const result = ref<FreeClassroomsResponse | null>(null)

const query = reactive({
  building: '',
  date: '',
  timeSlot: '',
})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getFreeClassrooms({
      building: query.building || undefined,
      date: query.date || undefined,
      timeSlot: query.timeSlot || undefined,
    })
    if (res.code === 200 && res.data) {
      result.value = res.data
    } else {
      ElMessage.warning(res.msg || '查询失败')
    }
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.building = ''
  query.date = ''
  query.timeSlot = ''
  result.value = null
}
</script>

<style scoped>
.free-classrooms-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.search-form {
  margin-bottom: 16px;
}
.result-info {
  display: flex;
  justify-content: space-between;
  padding: 8px 0 16px 0;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}
.count {
  color: #409eff;
  font-weight: 600;
}
.classroom-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  padding-top: 16px;
}
.classroom-card {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
  transition: transform 0.15s;
}
.classroom-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.room-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.room-info {
  font-size: 13px;
  color: #909399;
  margin: 2px 0;
}
</style>