<template>
  <div class="repair-detail-page">
    <div v-loading="loading">
      <el-empty v-if="!loading && !detail" description="报修不存在" />

      <el-card v-else>
        <template #header>
          <div class="header-toolbar">
            <span class="title">📄 报修详情</span>
            <el-button type="text" @click="$router.back()">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
          </div>
        </template>

        <!-- 基本信息 -->
        <div class="detail-header">
          <h2 class="detail-title">{{ detail?.title }}</h2>
          <div class="detail-tags">
            <el-tag size="default" :type="getStatusTagType(detail?.statusCode || '')">
              {{ detail?.status }}
            </el-tag>
            <el-tag size="default" type="info">{{ detail?.type }}</el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="报修类型">{{ detail?.type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail?.status || '-' }}</el-descriptions-item>
          <el-descriptions-item label="宿舍楼">{{ detail?.building || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ detail?.room || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail?.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预计完成">{{ detail?.expectedTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detail?.handler || '待分配' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(detail?.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态说明" :span="2">
            <span class="status-desc">{{ detail?.statusDesc || '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 问题描述 -->
        <el-divider content-position="left">问题描述</el-divider>
        <div class="description">{{ detail?.description }}</div>

        <!-- 图片 -->
        <div v-if="detail?.images?.length" class="images">
          <el-divider content-position="left">相关图片</el-divider>
          <div class="image-list">
            <el-image
              v-for="(url, index) in detail?.images"
              :key="index"
              :src="url"
              fit="cover"
              style="width: 120px; height: 90px; border-radius: 6px; cursor: pointer;"
              :preview-src-list="detail?.images"
              :initial-index="index"
            />
          </div>
        </div>

        <!-- 处理进度 -->
        <div v-if="detail?.progress?.length" class="progress">
          <el-divider content-position="left">处理进度</el-divider>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in detail?.progress"
              :key="index"
              :timestamp="formatDateTime(item.time)"
              placement="top"
            >
              <div class="progress-content">
                <el-tag size="small" :type="index === detail?.progress.length - 1 ? 'primary' : 'info'">
                  {{ item.status }}
                </el-tag>
                <span>{{ item.content }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 已完成时显示评价入口 -->
        <div v-if="detail?.statusCode === 'COMPLETED'" class="action-bar">
          <el-divider />
          <el-button
            v-if="!detail?.isEvaluated"
            type="warning"
            size="large"
            @click="openEvaluateDialog"
          >
            ⭐ 评价维修服务
          </el-button>
          <el-tag v-else type="success" size="large">已评价</el-tag>
        </div>
      </el-card>
    </div>

    <!-- ===== 评价弹窗 ===== -->
    <el-dialog v-model="evaluateDialogVisible" title="评价维修服务" width="460px">
      <el-form :model="evaluateForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate
            v-model="evaluateForm.score"
            :texts="['很差', '较差', '一般', '满意', '很满意']"
            show-text
          />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="evaluateForm.content"
            type="textarea"
            :rows="3"
            placeholder="请说说维修服务的体验"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluateLoading" @click="confirmEvaluate">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getRepairDetail, evaluateRepair } from '@/api/repair'
import type { RepairDetail } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<RepairDetail | null>(null)

// ===== 评价 =====
const evaluateDialogVisible = ref(false)
const evaluateLoading = ref(false)
const evaluateForm = ref({
  score: 5,
  content: '',
})

function getStatusTagType(statusCode: string) {
  const map: Record<string, 'warning' | 'danger' | 'success' | 'info'> = {
    PENDING: 'warning',
    PROCESSING: 'info',      
    COMPLETED: 'success',
    CANCELLED: 'danger',
  }
  return map[statusCode] || 'info'
}

// ===== 获取详情 =====
async function fetchDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getRepairDetail(String(id))
    if (res.code === 200 && res.data) {
      detail.value = res.data
    } else {
      ElMessage.warning(res.msg || '获取详情失败')
    }
  } finally {
    loading.value = false
  }
}

// ===== 打开评价弹窗 =====
function openEvaluateDialog() {
  evaluateForm.value = { score: 5, content: '' }
  evaluateDialogVisible.value = true
}

// ===== 提交评价 =====
async function confirmEvaluate() {
  if (!detail.value) return
  if (!evaluateForm.value.score) {
    ElMessage.warning('请评分')
    return
  }

  evaluateLoading.value = true
  try {
    const res = await evaluateRepair({
      repairId: detail.value.id,
      score: evaluateForm.value.score,
      content: evaluateForm.value.content || '无额外评价',
    })
    if (res.code === 200) {
      ElMessage.success('评价成功')
      evaluateDialogVisible.value = false
      await fetchDetail()
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  } catch {
    ElMessage.error('评价失败，请重试')
  } finally {
    evaluateLoading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.repair-detail-page {
  max-width: 1000px;
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

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

.detail-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.detail-tags {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 4px;
}

.status-desc {
  color: #409eff;
}

.description {
  padding: 8px 0;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.image-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.progress-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-bar {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}
</style>