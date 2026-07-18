<template>
  <div class="route-planner-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">🗺️ 路径规划</span>
          <el-button type="text" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </template>

      <div class="route-form">
        <el-form :model="form" label-width="80px" size="large">
          <el-form-item label="起点">
            <el-input
              v-model="form.fromName"
              placeholder="请输入起点（或点击选择当前位置）"
            />
            <el-button type="primary" plain @click="setCurrentLocation">
              当前位置
            </el-button>
          </el-form-item>

          <el-form-item label="终点">
            <el-input
              v-model="form.toName"
              placeholder="请输入终点"
              disabled
            />
          </el-form-item>

          <el-form-item label="出行方式">
            <el-radio-group v-model="form.mode">
              <el-radio label="walk">步行</el-radio>
              <el-radio label="drive">驾车</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handlePlanRoute">
              规划路线
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 路径结果 -->
      <div v-if="result" class="route-result">
        <el-divider content-position="left">路径结果</el-divider>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="距离">
            {{ result.distance }} 米
          </el-descriptions-item>
          <el-descriptions-item label="预计时间">
            {{ result.duration }} 分钟
          </el-descriptions-item>
          <el-descriptions-item label="出行方式">
            {{ result.mode }}
          </el-descriptions-item>
          <el-descriptions-item label="起点">
            {{ result.start?.name || '未知' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">导航步骤</el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in result.steps"
            :key="index"
            :timestamp="`${step.distance}米`"
            placement="top"
          >
            <span>{{ step.instruction }}</span>
            <span style="color: #909399; margin-left: 8px;">方向：{{ step.direction }}</span>
          </el-timeline-item>
        </el-timeline>
      </div>

      <el-empty v-else-if="!loading && hasSearched" description="暂无路径结果" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { planRoute } from '@/api/location'
import type { RoutePlanResponse } from '@/api/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const hasSearched = ref(false)
const result = ref<RoutePlanResponse | null>(null)

const form = reactive({
  fromLng: 0,
  fromLat: 0,
  fromName: '',
  toLng: 0,
  toLat: 0,
  toName: '',
  mode: 'walk' as 'walk' | 'drive',
})

// ===== 从查询参数获取终点 =====
function getParams() {
  const toLng = route.query.toLng
  const toLat = route.query.toLat
  const toName = route.query.toName

  if (toLng && toLat) {
    form.toLng = parseFloat(toLng as string)
    form.toLat = parseFloat(toLat as string)
    form.toName = (toName as string) || '目标地点'
  }
}

// ===== 设置当前位置（模拟：使用固定坐标） =====
function setCurrentLocation() {
  // 模拟获取当前位置（实际应使用 Geolocation API）
  // 这里使用教学楼A的坐标作为演示
  form.fromLng = 113.3245
  form.fromLat = 23.1000
  form.fromName = '当前所在位置（教学楼A）'
  ElMessage.success('已获取当前位置')
}

// ===== 规划路线 =====
async function handlePlanRoute() {
  if (!form.toLng || !form.toLat) {
    ElMessage.warning('请先选择终点')
    return
  }
  if (!form.fromLng || !form.fromLat) {
    ElMessage.warning('请先选择起点')
    return
  }

  loading.value = true
  hasSearched.value = true
  try {
    const res = await planRoute({
      fromLng: form.fromLng,
      fromLat: form.fromLat,
      toLng: form.toLng,
      toLat: form.toLat,
      mode: form.mode,
    })
    if (res.code === 200 && res.data) {
      result.value = res.data
    } else {
      ElMessage.error(res.msg || '规划失败')
    }
  } catch {
    ElMessage.error('规划失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getParams()
})
</script>

<style scoped>
.route-planner-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.route-form {
  padding: 8px 0;
}

.route-form .el-button {
  margin-top: 4px;
}

.route-result {
  margin-top: 16px;
}
</style>