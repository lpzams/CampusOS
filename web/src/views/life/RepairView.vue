<script setup lang="ts">
/**
 * 校园报修页（功能 11）。
 *
 * 左边报修列表 + 提交报修按钮（弹窗表单），右边选中工单的详情/进度，
 * 「已完成」的工单可以评价（弹窗打分 + 留言）。
 * 对应后端 /api/repair/submit、/list、/detail/{id}、/evaluate。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  REPAIR_STATUS_TAGS, REPAIR_TYPES, evaluateRepair, getRepairDetail, getRepairList, submitRepair,
} from '@/api/repair'
import type { RepairForm, RepairItem } from '@/api/repair'
import { formatDateTime } from '@/utils/format'
import ServiceHero from '@/components/ServiceHero.vue'

const loading = ref(false)
const list = ref<RepairItem[]>([])
const current = ref<RepairItem | null>(null)

async function fetchList() {
  loading.value = true
  try {
    list.value = await getRepairList()
    // 默认选中第一条，方便看到详情区
    if (!current.value && list.value.length > 0) selectRepair(list.value[0])
  } finally {
    loading.value = false
  }
}

async function selectRepair(row: RepairItem) {
  current.value = await getRepairDetail(row.id)
}

// ===== 提交报修 =====
const submitVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<RepairForm>({
  type: '', title: '', description: '', building: '', room: '', contactPhone: '',
})
const rules: FormRules = {
  type: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  description: [{ required: true, message: '请描述故障情况', trigger: 'blur' }],
  building: [{ required: true, message: '请输入楼栋', trigger: 'blur' }],
  room: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

function openSubmit() {
  Object.assign(form, { type: '', title: '', description: '', building: '', room: '', contactPhone: '' })
  submitVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await submitRepair(form)
    ElMessage.success('报修已提交')
    submitVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ===== 评价 =====
const evalVisible = ref(false)
const evaluating = ref(false)
const evalForm = reactive({ score: 5, content: '' })

function openEval() {
  evalForm.score = 5
  evalForm.content = ''
  evalVisible.value = true
}

async function handleEval() {
  if (!current.value) return
  evaluating.value = true
  try {
    const updated = await evaluateRepair({ repairId: current.value.id, score: evalForm.score, content: evalForm.content })
    ElMessage.success('评价成功，感谢反馈')
    evalVisible.value = false
    current.value = updated
    fetchList()
  } finally {
    evaluating.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="repair-layout">
    <ServiceHero
      class="repair-hero"
      eyebrow="SERVICE DESK"
      title="校园报修"
      description="从提交、受理到维修完成全程跟踪，常见水电与设施问题可在线快速报修。"
      icon="⚒"
      tone="orange"
      :metrics="[
        { label: '全部工单', value: list.length },
        { label: '处理中', value: list.filter(item => item.status === '处理中').length },
        { label: '已完成', value: list.filter(item => item.status === '已完成').length },
      ]"
    >
      <template #actions><el-button type="primary" plain @click="openSubmit">新建报修</el-button></template>
    </ServiceHero>
    <!-- 列表 -->
    <div class="left-col">
      <el-card shadow="never">
        <div class="toolbar">
          <span class="card-title">我的报修</span>
          <el-button type="primary" size="small" @click="openSubmit">提交报修</el-button>
        </div>
        <div v-loading="loading" class="repair-list">
          <el-empty v-if="!loading && list.length === 0" description="暂无报修记录" :image-size="80" />
          <div
            v-for="item in list"
            :key="item.id"
            class="repair-item"
            :class="{ active: current?.id === item.id }"
            @click="selectRepair(item)"
          >
            <div class="repair-item-head">
              <span class="repair-item-title">{{ item.title }}</span>
              <el-tag size="small" :type="REPAIR_STATUS_TAGS[item.status || '待处理'] || 'info'">{{ item.status }}</el-tag>
            </div>
            <div class="repair-item-meta">
              <span>{{ item.type }}</span>
              <span>{{ formatDateTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 详情 -->
    <div class="right-col">
      <el-card v-if="current" shadow="never">
        <template #header>
          <div class="detail-header">
            <span class="detail-title">{{ current.title }}</span>
            <el-tag :type="REPAIR_STATUS_TAGS[current.status || '待处理'] || 'info'">{{ current.status }}</el-tag>
          </div>
        </template>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="报修类型">{{ current.type }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ current.building }} {{ current.room }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ current.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="故障描述">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item v-if="current.statusDesc" label="处理进度">{{ current.statusDesc }}</el-descriptions-item>
          <el-descriptions-item v-if="current.score" label="评价">
            <el-rate :model-value="current.score" disabled />
            <div class="eval-content">{{ current.evaluation }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="current.status === '已完成'" class="detail-actions">
          <el-button type="primary" @click="openEval">评价维修服务</el-button>
        </div>
      </el-card>

      <el-empty v-else description="选择左侧工单查看详情" />
    </div>

    <!-- 提交报修弹窗 -->
    <el-dialog v-model="submitVisible" title="提交报修" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option v-for="t in REPAIR_TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="如：宿舍灯坏了" />
        </el-form-item>
        <el-form-item label="楼栋" prop="building">
          <el-input v-model="form.building" placeholder="如：6栋" />
        </el-form-item>
        <el-form-item label="房间" prop="room">
          <el-input v-model="form.room" placeholder="如：302室" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" maxlength="11" />
        </el-form-item>
        <el-form-item label="故障描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述故障情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="evalVisible" title="评价维修服务" width="420px">
      <div class="eval-form">
        <span class="eval-label">满意度评分</span>
        <el-rate v-model="evalForm.score" />
        <el-input v-model="evalForm.content" type="textarea" :rows="3" placeholder="说说这次维修体验（选填）" class="eval-input" />
      </div>
      <template #footer>
        <el-button @click="evalVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluating" @click="handleEval">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.repair-layout {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 16px;
  align-items: start;
}

.repair-hero {
  grid-column: 1 / -1;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title {
  font-weight: 600;
}

.repair-list {
  min-height: 200px;
}

.repair-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.repair-item:hover {
  border-color: #7c5cd6;
}

.repair-item.active {
  border-color: #7c5cd6;
  background: #f4effd;
}

.repair-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.repair-item-title {
  font-weight: 600;
  color: #303133;
}

.repair-item-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
}

.detail-actions {
  margin-top: 16px;
}

.eval-content {
  margin-top: 6px;
  color: #606266;
}

.eval-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eval-label {
  color: #606266;
}

.eval-input {
  margin-top: 4px;
}

@media (max-width: 768px) {
  .repair-layout {
    grid-template-columns: 1fr;
  }
}
</style>
