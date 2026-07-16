<script setup>
/**
 * 新闻详情页 —— 「带参数跳转」示例。
 *
 * 首页 uni.navigateTo('/pages/news/detail?id=123') 跳过来，
 * 在 onLoad(options) 里取到 options.id，再调详情接口（后端会自动 +1 浏览量）。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNewsDetail } from '@/api/news'
import { formatDateTime } from '@/utils/format'

const news = ref(null)
const failed = ref(false)

onLoad(async (options) => {
  try {
    news.value = await getNewsDetail(options.id)
  } catch (e) {
    // 错误提示已由 utils/request.js 统一 toast，这里只标记状态用于兜底展示
    failed.value = true
  }
})
</script>

<template>
  <view class="page">
    <view v-if="news" class="detail-card">
      <view class="detail-title">{{ news.title }}</view>
      <view class="detail-meta">
        <text class="detail-tag">{{ news.category }}</text>
        <text>{{ news.author }}</text>
        <text>{{ formatDateTime(news.publishedAt) }}</text>
        <text>{{ news.viewCount }} 次浏览</text>
      </view>
      <view class="divider" />
      <!-- 正文是纯文本，pre-wrap 保留发布时的换行 -->
      <view class="detail-content">{{ news.content }}</view>
    </view>

    <view v-else-if="failed" class="empty">新闻不存在或已下线</view>
    <view v-else class="empty">加载中…</view>
  </view>
</template>

<style scoped>
.page {
  padding: 20rpx;
}

.detail-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
}

.detail-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20rpx;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #909399;
}

.detail-tag {
  color: #2f54eb;
  background-color: rgba(47, 84, 235, 0.08);
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
}

.divider {
  height: 1rpx;
  background-color: #ebeef5;
  margin: 24rpx 0;
}

.detail-content {
  white-space: pre-wrap;
  font-size: 28rpx;
  color: #303133;
  line-height: 1.8;
}

.empty {
  text-align: center;
  color: #909399;
  padding: 120rpx 0;
  font-size: 26rpx;
}
</style>
