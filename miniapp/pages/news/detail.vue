<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNewsDetail } from '@/api/news'
import { favoriteNews, getFavoriteNews, unfavoriteNews } from '@/api/campus'
import { formatDateTime } from '@/utils/format'

const news = ref(null)
const failed = ref(false)

async function toggleFavorite() {
  if (!uni.getStorageSync('token')) return uni.navigateTo({ url: '/pages/auth/index' })
  await (news.value.isFavorite ? unfavoriteNews(news.value.id) : favoriteNews(news.value.id))
  news.value.isFavorite = !news.value.isFavorite
  news.value.favoriteCount = Math.max(0, (news.value.favoriteCount || 0) + (news.value.isFavorite ? 1 : -1))
  uni.showToast({ title: news.value.isFavorite ? '已收藏' : '已取消收藏' })
}

onLoad(async ({ id }) => {
  if (!id) {
    failed.value = true
    return
  }
  try {
    const data = await getNewsDetail(id)
    if (!data) throw new Error('新闻不存在')
    news.value = data
    if (uni.getStorageSync('token')) {
      const favorites = await getFavoriteNews().catch(() => [])
      news.value.isFavorite = favorites.some((item) => String(item.id) === String(data.id))
    }
    uni.setNavigationBarTitle({ title: data.title || '新闻详情' })
  } catch (_) {
    failed.value = true
  }
})
</script>

<template>
  <view class="page">
    <view v-if="news" class="article">
      <image v-if="news.coverImage" class="hero" :src="news.coverImage" mode="widthFix" />
      <view class="article-main">
        <view class="title">{{ news.title }}</view>
        <view class="meta">
          <text class="tag">{{ news.category }}</text>
          <text>{{ news.author }}</text>
          <text>{{ formatDateTime(news.createTime) }}</text>
          <text>{{ news.viewCount || 0 }} 阅读</text>
        </view>
        <view class="divider" />
        <rich-text class="content" :nodes="news.content" />
        <button class="favorite" @click="toggleFavorite">{{ news.isFavorite ? '取消收藏' : '收藏新闻' }} · {{ news.favoriteCount || 0 }}</button>
        <view v-if="news.updateTime" class="updated">更新于 {{ formatDateTime(news.updateTime) }}</view>
      </view>
    </view>

    <view v-else-if="failed" class="state">
      <text class="state-title">内容暂不可用</text>
      <text class="state-tip">新闻可能已删除或下线</text>
    </view>
    <view v-else class="state">加载中...</view>
  </view>
</template>

<style scoped lang="scss">
.page { min-height: 100vh; background: #fff; }
.article { display: block; }
.hero { display: block; width: 100%; max-height: 440rpx; background: #f5f7fa; }
.article-main { padding: 36rpx 32rpx 64rpx; }
.title { color: $campus-text-main; font-size: 42rpx; font-weight: 700; line-height: 1.45; }
.meta { display: flex; flex-wrap: wrap; gap: 12rpx 22rpx; margin-top: 22rpx; color: $campus-text-muted; font-size: 23rpx; }
.tag { padding: 2rpx 12rpx; border: 1rpx solid #d9ecff; border-radius: 4rpx; background: #ecf5ff; color: $campus-brand; }
.divider { height: 1rpx; margin: 30rpx 0; background: #ebeef5; }
.content { color: $campus-text-main; font-size: 29rpx; line-height: 1.9; overflow-wrap: anywhere; }
.updated { margin-top: 46rpx; color: $campus-text-muted; font-size: 23rpx; text-align: right; }
.favorite { width: 240rpx; margin: 42rpx auto 0; border: 1rpx solid $campus-brand; border-radius: 6rpx; background: #fff; color: $campus-brand; font-size: 26rpx; }
.state { display: flex; align-items: center; flex-direction: column; padding: 220rpx 0; color: $campus-text-muted; font-size: 26rpx; }
.state-title { color: $campus-text-main; font-size: 30rpx; }
.state-tip { margin-top: 12rpx; }
</style>
