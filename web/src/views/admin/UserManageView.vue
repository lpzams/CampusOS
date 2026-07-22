<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditUser, deactivateUser, enableAllUsers, pageUsers, updateUserRole, updateUserStatus } from '@/api/adminUser'
import type { ManagedUser } from '@/api/adminUser'

const loading = ref(false)
const enablingAll = ref(false)
const users = ref<ManagedUser[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', userType: undefined as number | undefined, status: undefined as number | undefined })
const ROLE_LABEL: Record<number, string> = { 1: '学生', 2: '教师', 3: '管理员' }
const STATUS_LABEL: Record<number, string> = { 0: '正常', 1: '禁用', 2: '待审核', 3: '已注销' }

async function load() {
  loading.value = true
  try {
    const data = await pageUsers(query)
    users.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}

async function changeRole(row: ManagedUser, userType: 1 | 2 | 3) {
  await updateUserRole(row.userId, userType)
  ElMessage.success('角色已更新，用户下次登录时生效')
  load()
}

async function changeStatus(row: ManagedUser, status: 0 | 1) {
  await updateUserStatus(row.userId, status)
  ElMessage.success(status === 0 ? '账号已启用' : '账号已禁用')
  load()
}

async function audit(row: ManagedUser, status: 0 | 1) {
  await auditUser(row.userId, status)
  ElMessage.success(status === 0 ? '审核通过' : '已拒绝并禁用')
  load()
}

async function enableAll() {
  const confirmed = await ElMessageBox.confirm(
    '将启用全部“禁用”和“待审核”账号；已注销账号不会恢复。待认领教师账户仍需设置独立密码后才能登录。',
    '一键启用账号',
    { type: 'warning', confirmButtonText: '确认启用', cancelButtonText: '取消' },
  ).catch(() => false)
  if (!confirmed) return
  enablingAll.value = true
  try {
    const result = await enableAllUsers()
    ElMessage.success(result.enabledCount ? `已启用 ${result.enabledCount} 个账号` : '没有需要启用的账号')
    load()
  } finally { enablingAll.value = false }
}

async function remove(row: ManagedUser) {
  const ok = await ElMessageBox.confirm(`确认注销账号「${row.username}」？`, '注销用户', { type: 'warning' }).catch(() => false)
  if (!ok) return
  await deactivateUser(row.userId)
  ElMessage.success('账号已注销')
  load()
}

function search() { query.pageNum = 1; load() }
onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header><span>用户与角色管理</span></template>
    <div class="filters">
      <el-input v-model="query.keyword" clearable placeholder="姓名、账号、手机号或学号" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.userType" clearable placeholder="全部角色" @change="search">
        <el-option label="学生" :value="1" /><el-option label="教师" :value="2" /><el-option label="管理员" :value="3" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="全部状态" @change="search">
        <el-option label="正常" :value="0" /><el-option label="禁用" :value="1" /><el-option label="待审核" :value="2" /><el-option label="已注销" :value="3" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" :loading="enablingAll" @click="enableAll">一键启用账号</el-button>
    </div>
    <el-table v-loading="loading" :data="users" stripe>
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="department" label="院系/部门" min-width="130" />
      <el-table-column label="角色" width="150"><template #default="{ row }"><el-select :model-value="row.userType" size="small" @change="changeRole(row as ManagedUser, $event as 1 | 2 | 3)"><el-option v-for="(_, value) in ROLE_LABEL" :key="value" :label="ROLE_LABEL[Number(value)]" :value="Number(value)" /></el-select></template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : row.status === 2 ? 'warning' : 'info'">{{ STATUS_LABEL[row.status] }}</el-tag></template></el-table-column>
      <el-table-column label="操作" min-width="250" fixed="right"><template #default="{ row }">
        <el-button v-if="row.status === 2" link type="success" @click="audit(row as ManagedUser, 0)">通过</el-button>
        <el-button v-if="row.status === 2" link type="danger" @click="audit(row as ManagedUser, 1)">拒绝</el-button>
        <el-button v-if="row.status === 0" link type="warning" @click="changeStatus(row as ManagedUser, 1)">禁用</el-button>
        <el-button v-if="row.status === 1" link type="success" @click="changeStatus(row as ManagedUser, 0)">启用</el-button>
        <el-button v-if="row.status !== 3" link type="danger" @click="remove(row as ManagedUser)">注销</el-button>
      </template></el-table-column>
    </el-table>
    <div class="pagination"><el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" background @current-change="load" /></div>
  </el-card>
</template>

<style scoped>
.filters { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filters .el-input { width: 260px; }.filters .el-select { width: 130px; }
.pagination { display: flex; justify-content: center; margin-top: 16px; }
</style>
