<script setup>
/**
 * 小程序首页 —— 最新新闻列表（对应功能文档第 3 条「小程序：首页展示最新新闻」）。
 *
 * 交互与网站列表页的差异（移动端习惯）：
 *  - 分页不用页码，而是「下拉刷新 + 上拉加载更多」；
 *  - 栏目筛选做成横向标签；
 *  - 点卡片 uni.navigateTo 跳详情页。
 *
 * 【新增功能时】做小程序列表页照抄本文件：换 api 函数和展示字段，
 * 并记得去 pages.json 注册新页面。
 */
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { NEWS_CATEGORIES, pageNews } from '@/api/news'
import { formatDateTime, summary } from '@/utils/format'

// 栏目标签：首位加一个「全部」表示不筛选
const categories = ['全部', ...NEWS_CATEGORIES]
const activeCategory = ref('全部')
const keyword = ref('')

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)

// 已加载条数追平 total 就没有更多了（首次加载前 total=0 也视为"没有更多"，不影响展示）
const finished = computed(() => list.value.length >= total.value)

/**
 * 拉取一页数据。
 * @param reset true=从第一页重来（进页面/搜索/切栏目/下拉刷新），false=追加下一页
 */
async function loadPage(reset) {
  if (loading.value) return
  loading.value = true
  try {
    if (reset) pageNum.value = 1
    const page = await pageNews({
      pageNum: pageNum.value,
      pageSize,
      keyword: keyword.value,
      category: activeCategory.value === '全部' ? '' : activeCategory.value,
    })
    list.value = reset ? page.list : list.value.concat(page.list)
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function selectCategory(c) {
  activeCategory.value = c
  loadPage(true)
}

function handleSearch() {
  loadPage(true)
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
}

// ===== 页面生命周期（来自 @dcloudio/uni-app，不是 vue 的 onMounted） =====
onLoad(() => {
  loadPage(true)
})

// 下拉刷新：pages.json 里该页面开了 enablePullDownRefresh 才会触发
onPullDownRefresh(async () => {
  try {
    await loadPage(true)
  } finally {
    uni.stopPullDownRefresh()
  }
})

// 滚动到底部：加载下一页
onReachBottom(() => {
  if (finished.value || loading.value) return
  pageNum.value += 1
  loadPage(false)
})
</script>

<template>
  <view class="page">
    <!-- 搜索框 -->
    <view class="search-box">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索新闻标题…"
        confirm-type="search"
        @confirm="handleSearch"
      />
    </view>

    <!-- 栏目横向标签 -->
    <scroll-view scroll-x class="category-bar">
      <view
        v-for="c in categories"
        :key="c"
        class="category-tag"
        :class="{ active: c === activeCategory }"
        @click="selectCategory(c)"
      >
        {{ c }}
      </view>
    </scroll-view>

    <!-- 新闻列表 -->
    <view v-if="list.length === 0 && !loading" class="empty">
      暂无新闻，去网站后台发布一条试试～
    </view>

    <view
      v-for="item in list"
      :key="item.id"
      class="news-card"
      @click="goDetail(item.id)"
    >
      <view class="news-title">{{ item.title }}</view>
      <view class="news-summary">{{ summary(item.content) }}</view>
      <view class="news-meta">
        <text class="news-tag">{{ item.category }}</text>
        <text>{{ item.author }}</text>
        <text>{{ formatDateTime(item.publishedAt) }}</text>
      </view>
    </view>

    <!-- 底部加载状态 -->
    <view v-if="list.length > 0" class="load-more">
      {{ loading ? '加载中…' : finished ? '— 没有更多了 —' : '上拉加载更多' }}
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 20rpx;
}

.search-box {
  margin-bottom: 20rpx;
}

.search-input {
  background-color: #fff;
  border-radius: 40rpx;
  padding: 16rpx 30rpx;
  font-size: 28rpx;
}

.category-bar {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.category-tag {
  display: inline-block;
  padding: 10rpx 28rpx;
  margin-right: 16rpx;
  border-radius: 30rpx;
  background-color: #fff;
  color: #606266;
  font-size: 26rpx;
}

.category-tag.active {
  background-color: #2f54eb;
  color: #fff;
}

.news-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.news-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.news-summary {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #606266;
  line-height: 1.6;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #909399;
}

.news-tag {
  color: #2f54eb;
  background-color: rgba(47, 84, 235, 0.08);
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
}

.empty {
  text-align: center;
  color: #909399;
  padding: 120rpx 0;
  font-size: 26rpx;
}

.load-more {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 24rpx 0;
}
</style>
