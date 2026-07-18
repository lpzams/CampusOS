<template>
  <div class="my-orders-page">
    <el-card>
      <template #header>
        <span class="title">📋 我的订单</span>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && !orders.length" description="暂无订单" />

        <div v-else class="order-list">
          <div
            v-for="item in orders"
            :key="item.orderId"
            class="order-item"
          >
            <div class="order-header">
              <span class="order-id">订单号：{{ item.orderId }}</span>
              <!-- ✅ :type 使用辅助函数，删除错误的 size 属性 -->
              <el-tag :type="getOrderStatusTagType(item.statusCode)" size="small">
                {{ item.status }}
              </el-tag>
            </div>
            <div class="order-body">
              <span class="order-product">{{ item.productTitle }}</span>
              <span class="order-price">¥{{ item.price.toFixed(2) }}</span>
            </div>
            <div class="order-footer">
              <span class="order-time">📅 {{ formatDateTime(item.createTime) }}</span>
              <span v-if="item.message" class="order-message">💬 {{ item.message }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const orders = ref<any[]>([])

// ✅ 辅助函数：返回安全的字面量类型
function getOrderStatusTagType(statusCode: string) {
  const map: Record<string, 'warning' | 'primary' | 'success' | 'danger' | 'info'> = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger',
  }
  return map[statusCode] || 'info'
}

async function fetchOrders() {
  loading.value = true
  try {
    // TODO: 待后端实现 /product/orders 接口
    // const res = await getMyOrders()
    // if (res.code === 200 && res.data) {
    //   orders.value = res.data
    // }
    // 临时 Mock
    orders.value = [
      {
        orderId: 'ORDER202406150001',
        productId: 1,
        productTitle: '二手显示器',
        price: 300.00,
        buyerId: 1,
        sellerId: 3,
        sellerPhone: '13900139000',
        status: '待确认',
        statusCode: 'PENDING',
        createTime: '2024-06-15 14:30:00',
        message: '请问显示器还在吗？',
      },
      {
        orderId: 'ORDER202406150002',
        productId: 2,
        productTitle: '大学英语教材全套',
        price: 80.00,
        buyerId: 1,
        sellerId: 5,
        sellerPhone: '13800138000',
        status: '已完成',
        statusCode: 'COMPLETED',
        createTime: '2024-06-10 10:00:00',
        message: '教材还在吗？',
      },
    ]
  } catch {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.my-orders-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  padding: 14px 18px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-id {
  font-size: 13px;
  color: #909399;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-product {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.order-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.order-footer {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>