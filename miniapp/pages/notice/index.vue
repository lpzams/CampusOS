<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getNotices } from '@/api/campus'
import { formatDateTime } from '@/utils/format'

const type = ref('')
const department = ref('')
const list = ref([])
const total = ref(0)
const page = ref(0)
const loading = ref(false)

async function load(reset = false) {
  if (loading.value || (!reset && page.value && list.value.length >= total.value)) return
  loading.value = true
  try {
    const next = reset ? 1 : page.value + 1
    const data = await getNotices({ type: type.value, department: department.value.trim(), page: next, size: 10 })
    const items = Array.isArray(data?.list) ? data.list : []
    list.value = reset ? items : list.value.concat(items); total.value = Number(data?.total) || 0; page.value = next
  } finally { loading.value = false }
}
function open(id) { uni.navigateTo({ url: `/pages/detail/index?type=notice&id=${id}` }) }
onLoad(() => load(true))
onPullDownRefresh(async () => { try { await load(true) } finally { uni.stopPullDownRefresh() } })
onReachBottom(() => load())
</script>
<template>
  <view class="screen">
    <view class="panel filters"><picker :range="['全部','学校','院系']" @change="type=['','SCHOOL','DEPT'][$event.detail.value];load(true)"><view class="field picker">范围：{{ {'':'全部',SCHOOL:'学校',DEPT:'院系'}[type] }}</view></picker><input v-model="department" class="field" placeholder="院系名称（可选）" confirm-type="search" @confirm="load(true)" /></view>
    <view v-for="item in list" :key="item.id" class="list-item" @click="open(item.id)"><view class="line"><text class="item-title">{{ item.title }}</text><text v-if="item.isRead === false" class="dot" /></view><view class="item-meta"><text>{{ item.type === 'DEPT' ? '院系公告' : '学校公告' }}</text><text>{{ item.department }}</text><text>{{ formatDateTime(item.createTime) }}</text></view></view>
    <view v-if="!list.length && !loading" class="empty-state">暂无公告</view><view v-if="loading" class="empty-state">加载中...</view>
  </view>
</template>
<style scoped lang="scss">.filters{padding-bottom:10rpx}.picker{line-height:76rpx}.line{display:flex;align-items:flex-start;gap:14rpx}.line .item-title{flex:1}.dot{width:14rpx;height:14rpx;margin-top:14rpx;border-radius:7rpx;background:#f56c6c}</style>
