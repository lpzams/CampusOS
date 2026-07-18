<template>
  <div class="payment-records-page">
    <el-card>
      <template #header>
        <div class="header-toolbar">
          <span class="title">📋 缴费记录</span>
          <span class="info-text">共 {{ total }} 条记录</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !records.length" description="暂无缴费记录" />

        <el-table v-else :data="records" stripe style="width: 100%">
          <el-table-column prop="orderId" label="订单号" width="180" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="amount" label="金额" width="120" align="center">
            <template #default="{ row }">
              <span style="font-weight: 600; color: #f56c6c;">
                ¥{{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="payMethod" label="支付方式" width="120" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '已缴费' ? 'success' : 'warning'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="payTime" label="支付时间" width="180" align="center" />
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
import { getPaymentRecords } from '@/api/payment'
import type { PaymentRecord } from '@/api/types'

const loading = ref(false)
const records = ref<PaymentRecord[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getPaymentRecords({
      page: query.page,
      size: query.size,
    })
    if (res.code === 200 && res.data) {
      records.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.payment-records-page {
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

.info-text {
  color: #909399;
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>