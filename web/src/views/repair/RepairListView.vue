<template>
  <div class="repair-list-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📋 报修记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="$router.push('/repair/submit')">
              + 提交报修
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !list.length" description="暂无报修记录" />

        <div v-else class="repair-list">
          <div
            v-for="item in list"
            :key="item.id"
            class="repair-item"
            @click="$router.push(`/repair/detail/${item.id}`)"
          >
            <div class="repair-header">
              <div class="repair-title">
                <span class="title-text">{{ item.title }}</span>
                <el-tag size="small" :type="getStatusTagType(item.statusCode)">
                  {{ item.status }}
                </el-tag>
              </div>
              <span class="repair-type">{{ item.type }}</span>
            </div>

            <div class="repair-body">
              <div class="repair-info">
                <span class="info-label">📅 提交时间</span>
                <span>{{ formatDateTime(item.createTime) }}</span>
              </div>
              <div class="repair-info">
                <span class="info-label">⏰ 预计完成</span>
                <span>{{ item.expectedTime || '-' }}</span>
              </div>
              <div v-if="item.handler" class="repair-info">
                <span class="info-label">👨‍🔧 处理人</span>
                <span>{{ item.handler }}</span>
              </div>
              <div v-if="item.statusDesc" class="repair-info full-width">
                <span class="info-label">📌 状态说明</span>
                <span class="status-desc">{{ item.statusDesc }}</span>
              </div>
            </div>

            <div v-if="item.statusCode === 'COMPLETED'" class="repair-footer">
              <el-button
                v-if="!item.isEvaluated"
                type="warning"
                size="small"
                @click.stop="openEvaluateDialog(item.id)"
              >
                去评价
              </el-button>
              <el-tag v-else type="success" size="small">已评价</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

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
import { ElMessage } from 'element-plus'
import { getRepairList, evaluateRepair } from '@/api/repair'
import type { RepairListItem } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref<RepairListItem[]>([])

// ===== 评价 =====
const evaluateDialogVisible = ref(false)
const evaluateLoading = ref(false)
const currentRepairId = ref<number | null>(null)
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

// ===== 获取列表 =====
async function fetchList() {
  loading.value = true
  try {
    const res = await getRepairList()
    if (res.code === 200 && res.data) {
      list.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// ===== 打开评价弹窗 =====
function openEvaluateDialog(id: number) {
  currentRepairId.value = id
  evaluateForm.value = { score: 5, content: '' }
  evaluateDialogVisible.value = true
}

// ===== 提交评价 =====
async function confirmEvaluate() {
  if (!currentRepairId.value) return
  if (!evaluateForm.value.score) {
    ElMessage.warning('请评分')
    return
  }

  evaluateLoading.value = true
  try {
    const res = await evaluateRepair({
      repairId: currentRepairId.value,
      score: evaluateForm.value.score,
      content: evaluateForm.value.content || '无额外评价',
    })
    if (res.code === 200) {
      ElMessage.success('评价成功')
      evaluateDialogVisible.value = false
      await fetchList()
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  } catch {
    ElMessage.error('评价失败，请重试')
  } finally {
    evaluateLoading.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.repair-list-page {
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

.repair-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.repair-item {
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.repair-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.repair-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.repair-type {
  font-size: 13px;
  color: #909399;
}

.repair-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 24px;
}

.repair-info {
  display: flex;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.repair-info .info-label {
  color: #909399;
}

.repair-info.full-width {
  grid-column: 1 / -1;
}

.status-desc {
  color: #409eff;
}

.repair-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}
</style>