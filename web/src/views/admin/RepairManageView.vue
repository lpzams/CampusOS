<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminRepairs, updateRepairProgress } from '@/api/repair'
import type { RepairItem } from '@/api/repair'
const list = ref<RepairItem[]>([]); const loading = ref(false)
async function load(){loading.value=true;try{list.value=await getAdminRepairs()}finally{loading.value=false}}
async function setStatus(row: RepairItem, status: '处理中'|'已完成'){await updateRepairProgress(row.id,{status,statusDesc:status==='处理中'?'维修人员已接单，正在处理':'维修已完成，等待学生评价'});ElMessage.success('工单状态已更新');load()}
onMounted(load)
</script>
<template><el-card shadow="never"><template #header><div class="head"><span>报修工单管理</span><el-button @click="load">刷新</el-button></div></template><el-table v-loading="loading" :data="list"><el-table-column prop="title" label="问题" min-width="160"/><el-table-column prop="building" label="位置" width="140"><template #default="{row}">{{row.building}} {{row.room}}</template></el-table-column><el-table-column prop="contactPhone" label="联系电话" width="130"/><el-table-column prop="status" label="状态" width="100"/><el-table-column label="处理" width="180"><template #default="{row}"><el-button link type="primary" :disabled="row.status!=='待处理'" @click="setStatus(row as RepairItem,'处理中')">接单</el-button><el-button link type="success" :disabled="row.status!=='处理中'" @click="setStatus(row as RepairItem,'已完成')">完成</el-button></template></el-table-column></el-table></el-card></template>
<style scoped>.head{display:flex;justify-content:space-between;align-items:center}</style>
