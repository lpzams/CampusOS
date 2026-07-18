<template>
  <div class="card-transactions-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📋 消费记录</span>
          <span class="info-text">共 {{ total }} 条记录</span>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
          @change="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 交易列表 -->
      <div v-loading="loading">
        <el-empty v-if="!loading && !transactions.length" description="暂无消费记录" />

        <el-table v-else :data="transactions" stripe style="width: 100%">
          <el-table-column label="类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.typeCode === 'CONSUME' ? 'danger' : 'success'" size="small">
                {{ row.type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.amount > 0 ? '#67C23A' : '#F56C6C', fontWeight: 600 }">
                {{ row.amount > 0 ? '+' : '' }}{{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="120" align="center">
            <template #default="{ row }">
              <span>¥{{ row.balance.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="merchant" label="商户" width="140" />
          <el-table-column prop="description" label="描述" min-width="150" />
          <el-table-column prop="createTime" label="时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getTransactions } from '@/api/card'
import type { TransactionItem } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const transactions = ref<TransactionItem[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

const query = reactive({
  page: 1,
  size: 10,
  startDate: '',
  endDate: '',
})

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
    }
    if (query.startDate) params.startDate = query.startDate
    if (query.endDate) params.endDate = query.endDate

    const res = await getTransactions(params)
    if (res.code === 200 && res.data) {
      transactions.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (dateRange.value) {
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
  } else {
    query.startDate = ''
    query.endDate = ''
  }
  query.page = 1
  fetchData()
}

function handleReset() {
  dateRange.value = null
  query.startDate = ''
  query.endDate = ''
  query.page = 1
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.card-transactions-page {
  max-width: 1100px;
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

.info-text {
  color: #909399;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>