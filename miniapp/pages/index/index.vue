<script setup>
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getNewsCategories, pageNews } from '@/api/news'
import { getUnreadNoticeCount } from '@/api/campus'
import { formatDateTime } from '@/utils/format'

const categories = ref([])
const selectedId = ref('')
const selectedName = ref('')
const keyword = ref('')
const news = ref([])
const total = ref(0)
const page = ref(0)
const loading = ref(false)
const unread = ref(0)
const finished = computed(() => page.value > 0 && news.value.length >= total.value)
let version = 0
const services = [
  { label: '公告通知', mark: '告', path: 'notice/index', tone: 'violet' },
  { label: '课程成绩', mark: '课', path: 'academic/index', tone: 'blue' },
  { label: '校园生活', mark: '卡', path: 'life/index', tone: 'orange' },
  { label: '校园报修', mark: '修', path: 'repair/index', tone: 'green' },
  { label: '二手交易', mark: '市', path: 'market/index', tone: 'pink' },
  { label: '校园活动', mark: '动', path: 'activity/index', tone: 'orange' },
  { label: '校园地图', mark: '图', path: 'map/index', tone: 'blue' },
  { label: 'AI 助手', mark: 'AI', path: 'ai/index', tone: 'violet' },
]
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const categoryId = (item) => item?.id ?? item?.categoryId ?? item
const categoryName = (item) => item?.name ?? item?.categoryName ?? item

async function load(reset = false) {
  if (!reset && (loading.value || finished.value)) return
  const requestVersion = reset ? ++version : version
  const nextPage = reset ? 1 : page.value + 1
  loading.value = true

  try {
    const data = await pageNews({
      page: nextPage,
      size: 10,
      keyword: keyword.value.trim(),
      categoryId: selectedId.value,
      category: selectedName.value,
    })
    if (requestVersion !== version) return
    const items = Array.isArray(data?.list) ? data.list : []
    news.value = reset ? items : news.value.concat(items)
    total.value = Number(data?.total) || 0
    page.value = nextPage
  } catch (_) {
    // request.js has already shown the actionable error.
  } finally {
    if (requestVersion === version) loading.value = false
  }
}

function selectCategory(item = null) {
  selectedId.value = item ? categoryId(item) : ''
  selectedName.value = item ? categoryName(item) : ''
  load(true)
}

function openDetail(id) {
  uni.navigateTo({ url: `/pages/news/detail?id=${encodeURIComponent(id)}` })
}

// tabBar 页只能用 switchTab，且不能带 query；其它页用 navigateTo。
const tabPaths = ['index/index', 'services/index', 'ai/index', 'profile/index']
function openPage(path) {
  if (tabPaths.includes(path)) uni.switchTab({ url: `/pages/${path}` })
  else uni.navigateTo({ url: `/pages/${path}` })
}

onLoad(async () => {
  getNewsCategories()
    .then((data) => { categories.value = Array.isArray(data) ? data : [] })
    .catch(() => {})
  await load(true)
  if (uni.getStorageSync('token')) getUnreadNoticeCount().then(data => { unread.value = Number(data?.count ?? data) || 0 }).catch(() => {})
})
onPullDownRefresh(async () => {
  try { await load(true) } finally { uni.stopPullDownRefresh() }
})
onReachBottom(() => load())
</script>

<template>
  <view class="page">
    <view class="intro">
      <view class="glow glow-one" /><view class="glow glow-two" />
      <view class="intro-top"><text class="brand">CampusOS</text><text class="mine" @click="openPage(uni.getStorageSync('token') ? 'profile/index' : 'auth/index')">个人中心 ›</text></view>
      <text class="eyebrow">{{ greeting }}，欢迎回到智慧校园</text>
      <text class="headline">今天想做些什么？</text>
      <text class="hero-sub">资讯、教务与校园生活，一站快捷办理</text>
      <view class="search">
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索校园资讯"
          confirm-type="search"
          @confirm="load(true)"
        />
        <button class="search-button" @click="load(true)">搜索</button>
      </view>
    </view>

    <view class="section-head service-head">
      <view><text class="section-title">常用服务</text><text class="section-sub">高频事项，一键直达</text></view>
      <text class="more" @click="openPage('services/index')">全部服务 ›</text>
    </view>
    <view class="service-grid">
      <view v-for="item in services" :key="item.label" class="service" @click="openPage(item.path)">
        <text class="service-mark" :class="item.tone">{{ item.mark }}</text><text>{{ item.label }}</text>
        <text v-if="item.path === 'notice/index' && unread" class="badge">{{ unread }}</text>
      </view>
    </view>

    <scroll-view scroll-x class="categories" :show-scrollbar="false">
      <view class="category-row">
        <view class="category" :class="{ active: selectedId === '' }" @click="selectCategory()">全部</view>
        <view
          v-for="item in categories"
          :key="categoryId(item)"
          class="category"
          :class="{ active: selectedId === categoryId(item) }"
          @click="selectCategory(item)"
        >
          {{ categoryName(item) }}
        </view>
      </view>
    </scroll-view>

    <view class="section-head">
      <view><text class="section-title">校园资讯</text><text class="section-sub">掌握校园最新动态</text></view>
      <text class="count">{{ total }} 篇</text>
    </view>

    <view v-for="item in news" :key="item.id" class="news-item" @click="openDetail(item.id)">
      <image v-if="item.coverImage" class="cover" :src="item.coverImage" mode="aspectFill" />
      <view v-else class="cover fallback">校园</view>
      <view class="news-copy">
        <text class="news-title">{{ item.title }}</text>
        <text class="summary">{{ item.summary || '暂无摘要' }}</text>
        <view class="meta">
          <text class="tag">{{ item.category }}</text>
          <text>{{ formatDateTime(item.createTime) }}</text>
          <text>{{ item.viewCount || 0 }} 阅读</text>
        </view>
      </view>
    </view>

    <view v-if="!news.length && !loading" class="state">暂无新闻</view>
    <view v-else-if="loading" class="state">加载中...</view>
    <view v-else-if="finished" class="state compact">没有更多了</view>
  </view>
</template>

<style scoped lang="scss">
.page { min-height: 100vh; padding-bottom: 48rpx; }
.intro { position: relative; padding: 44rpx 30rpx 48rpx; overflow: hidden; background: linear-gradient(135deg, #241457 0%, #7352cc 55%, #b06fd0 100%); color: #fff; box-shadow: 0 18rpx 46rpx rgba(36,20,87,.22); }
.glow { position: absolute; border-radius: 50%; background: rgba(255,255,255,.12); filter: blur(2rpx); }
.glow-one { top: -110rpx; right: -70rpx; width: 310rpx; height: 310rpx; }
.glow-two { right: 210rpx; bottom: -130rpx; width: 230rpx; height: 230rpx; }
.intro-top { display: flex; align-items: center; justify-content: space-between; }
.brand { font-size: 27rpx; font-weight: 800; letter-spacing: 1rpx; }
.mine { position: relative; padding: 11rpx 20rpx; border: 1rpx solid rgba(255,255,255,.4); border-radius: 999rpx; background: rgba(255,255,255,.14); font-size: 23rpx; }
.eyebrow, .headline, .hero-sub { position: relative; display: block; }
.eyebrow { margin-top: 38rpx; color: #ffe9ad; font-size: 23rpx; font-weight: 700; }
.headline { margin-top: 9rpx; font-size: 43rpx; font-weight: 800; letter-spacing: 1rpx; }
.hero-sub { margin-top: 10rpx; color: rgba(255,255,255,.72); font-size: 24rpx; }
.search { position: relative; display: flex; height: 86rpx; margin-top: 32rpx; overflow: hidden; border: 1rpx solid rgba(255,255,255,.44); border-radius: 20rpx; background: rgba(255,255,255,.96); box-shadow: 0 12rpx 30rpx rgba(36,20,87,.2); }
.search-input { flex: 1; min-width: 0; height: 76rpx; padding: 0 22rpx; color: $campus-text-main; font-size: 27rpx; }
.search-button { width: 132rpx; height: 86rpx; margin: 0; border-radius: 0; background: #7c5cd6; color: #fff; font-size: 27rpx; font-weight: 600; line-height: 86rpx; }
.search-button::after { border: 0; }
.service-head { padding-bottom: 14rpx !important; }
.service-grid { display: grid; grid-template-columns: repeat(4, 1fr); margin: 0 24rpx 8rpx; padding: 14rpx 6rpx; border: 1rpx solid #ece4fb; border-radius: 24rpx; background: rgba(255,255,255,.96); box-shadow: 0 12rpx 36rpx rgba(124,92,214,.09); }
.service { display: flex; align-items: center; flex-direction: column; justify-content: center; height: 142rpx; background: transparent; color: #443a6e; font-size: 23rpx; }
.service { position: relative; }
.badge { position: absolute; top: 10rpx; right: 12rpx; min-width: 30rpx; height: 30rpx; padding: 0 7rpx; border-radius: 15rpx; background: #f56c6c; color: #fff; font-size: 19rpx; line-height: 30rpx; text-align: center; }
.service-mark { display: flex; align-items: center; justify-content: center; width: 62rpx; height: 62rpx; margin-bottom: 12rpx; border-radius: 18rpx; font-size: 24rpx; font-weight: 800; }
.service-mark.violet { background: #efe9fc; color: #694ab9; }.service-mark.blue { background: #e8f1ff; color: #3976c9; }.service-mark.orange { background: #fff0df; color: #c87528; }.service-mark.green { background: #e6f7f1; color: #288469; }.service-mark.pink { background: #faeaf3; color: #b95387; }
.categories { background: transparent; white-space: nowrap; }
.category-row { display: inline-flex; min-width: 100%; padding: 18rpx 24rpx; gap: 12rpx; }
.category { flex: 0 0 auto; padding: 13rpx 24rpx; border: 1rpx solid #dcd2f7; border-radius: 999rpx; background: #fff; color: $campus-text-secondary; }
.category.active { border-color: #7c5cd6; background: #7c5cd6; color: #fff; box-shadow: 0 6rpx 16rpx rgba(124,92,214,.2); }
.section-head { display: flex; align-items: center; justify-content: space-between; padding: 32rpx 24rpx 18rpx; }
.section-title { margin: 0; font-size: 32rpx; }
.section-sub { display: block; margin-top: 5rpx; color: $campus-text-muted; font-size: 22rpx; }
.more { color: #7c5cd6; font-size: 24rpx; }
.count { color: $campus-text-muted; font-size: 23rpx; }
.news-item { display: flex; min-height: 190rpx; margin: 0 24rpx 18rpx; padding: 20rpx; gap: 22rpx; border: 1rpx solid #ece4fb; border-radius: 22rpx; background: rgba(255,255,255,.96); box-shadow: 0 8rpx 28rpx rgba(124,92,214,.08); }
.cover { flex: 0 0 210rpx; width: 210rpx; height: 158rpx; border-radius: 10rpx; background: #f4effd; }
.fallback { display: flex; align-items: center; justify-content: center; color: #6247ab; font-size: 25rpx; font-weight: 600; }
.news-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; }
.news-title { display: -webkit-box; overflow: hidden; font-size: 30rpx; font-weight: 600; line-height: 1.45; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.summary { display: -webkit-box; overflow: hidden; margin-top: 8rpx; color: $campus-text-secondary; font-size: 24rpx; line-height: 1.45; -webkit-box-orient: vertical; -webkit-line-clamp: 1; }
.meta { display: flex; align-items: center; flex-wrap: wrap; margin-top: auto; gap: 8rpx 16rpx; color: $campus-text-muted; font-size: 21rpx; }
.tag { padding: 3rpx 13rpx; border-radius: 999rpx; background: #efe9fc; color: #6247ab; }
.state { padding: 120rpx 0; color: $campus-text-muted; text-align: center; }
.state.compact { padding: 32rpx 0 8rpx; font-size: 23rpx; }
</style>
