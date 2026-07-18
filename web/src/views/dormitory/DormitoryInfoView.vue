<template>
  <div class="dormitory-info-page">
    <el-row :gutter="20">
      <!-- 宿舍信息 -->
      <el-col :xs="24" :md="14">
        <el-card>
          <template #header>
            <span class="title">🏠 宿舍信息</span>
          </template>

          <div v-loading="loading">
            <el-empty v-if="!loading && !info" description="未获取到宿舍信息" />

            <div v-else class="dorm-info">
              <!-- 宿舍基本信息 -->
              <div class="dorm-header">
                <div class="dorm-title">{{ info?.building }} {{ info?.room }}</div>
                <el-tag size="large" type="primary">{{ info?.type }}</el-tag>
              </div>

              <!-- 设施 -->
              <div class="facilities">
                <span class="facilities-label">设施：</span>
                <el-tag
                  v-for="facility in info?.facilities || []"
                  :key="facility"
                  size="small"
                  type="info"
                  style="margin-right: 6px;"
                >
                  {{ facility }}
                </el-tag>
              </div>

              <el-divider />

              <!-- 水电余额 -->
              <div class="utility-info">
                <div class="utility-item">
                  <span class="utility-label">⚡ 电费余额</span>
                  <span class="utility-value" :class="info?.electricityBalance && info.electricityBalance < 20 ? 'text-warning' : ''">
                    ¥{{ info?.electricityBalance?.toFixed(2) || '0.00' }}
                  </span>
                </div>
                <div class="utility-item">
                  <span class="utility-label">💧 水费余额</span>
                  <span class="utility-value" :class="info?.waterBalance && info.waterBalance < 10 ? 'text-warning' : ''">
                    ¥{{ info?.waterBalance?.toFixed(2) || '0.00' }}
                  </span>
                </div>
              </div>

              <el-divider />

              <!-- 宿舍成员 -->
              <div class="members">
                <div class="members-header">
                  <span class="members-label">👥 宿舍成员</span>
                  <span class="members-count">共 {{ info?.members?.length || 0 }} 人</span>
                </div>
                <div class="member-list">
                  <div
                    v-for="member in info?.members || []"
                    :key="member.userId"
                    class="member-item"
                  >
                    <el-avatar :size="40" :src="member.avatar" />
                    <div class="member-info">
                      <span class="member-name">{{ member.realName }}</span>
                      <span class="member-phone">{{ member.phone }}</span>
                    </div>
                    <el-tag v-if="member.isRoomLeader" type="warning" size="small">舍长</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 操作 -->
      <el-col :xs="24" :md="10">
        <el-card>
          <template #header>
            <span class="title">🔧 快捷操作</span>
          </template>

          <div class="quick-actions">
            <el-button
              type="primary"
              plain
              size="large"
              @click="$router.push('/dormitory/utility')"
              style="width: 100%"
            >
              ⚡ 查看水电详情
            </el-button>
            <el-button
              type="info"
              plain
              size="large"
              @click="$router.push('/dormitory/notice')"
              style="width: 100%"
            >
              📢 宿舍公告
            </el-button>
            <el-button
              type="success"
              plain
              size="large"
              @click="refreshInfo"
              style="width: 100%"
            >
              🔄 刷新信息
            </el-button>
          </div>

          <div v-if="info?.electricityBalance && info.electricityBalance < 20" class="tip-warning">
            ⚠️ 电费余额不足 20 元，请及时充值！
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDormitoryInfo } from '@/api/dormitory'
import type { DormitoryInfo } from '@/api/types'

const loading = ref(false)
const info = ref<DormitoryInfo | null>(null)

async function fetchInfo() {
  loading.value = true
  try {
    const res = await getDormitoryInfo()
    if (res.code === 200 && res.data) {
      info.value = res.data
    } else {
      ElMessage.warning(res.msg || '获取宿舍信息失败')
    }
  } catch {
    ElMessage.error('获取宿舍信息失败')
  } finally {
    loading.value = false
  }
}

function refreshInfo() {
  fetchInfo()
}

onMounted(fetchInfo)
</script>

<style scoped>
.dormitory-info-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

/* ===== 宿舍信息 ===== */
.dorm-info {
  padding: 4px 0;
}

.dorm-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.dorm-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.facilities {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.facilities-label {
  font-size: 14px;
  color: #606266;
  margin-right: 4px;
}

/* ===== 水电余额 ===== */
.utility-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.utility-item {
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.utility-label {
  font-size: 13px;
  color: #909399;
}

.utility-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.text-warning {
  color: #e6a23c;
}

/* ===== 宿舍成员 ===== */
.members {
  margin-top: 4px;
}

.members-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.members-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.members-count {
  font-size: 13px;
  color: #909399;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.member-phone {
  font-size: 12px;
  color: #909399;
}

/* ===== 快捷操作 ===== */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px 0;
}

.tip-warning {
  margin-top: 12px;
  padding: 10px 12px;
  background: #fdf6ec;
  border-radius: 6px;
  color: #e6a23c;
  font-size: 13px;
  text-align: center;
}
</style>