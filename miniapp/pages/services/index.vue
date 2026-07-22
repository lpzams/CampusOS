<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUnreadNoticeCount } from '@/api/campus'

// 服务总目录：把 15 个功能按场景分组，点任意一项跳到对应页面。
// path 里带 ?tab= 的，是复用 academic/life 页已支持的分栏深链。
const groups = [
  {
    name: '教务学习',
    desc: '课程、成绩与考试安排',
    tone: 'violet',
    items: [
      { label: '课程课表', desc: '课表 · 今日课程 · 空教室', mark: '课', path: 'academic/index?tab=course' },
      { label: '成绩查询', desc: 'GPA · 统计分析', mark: '绩', path: 'academic/index?tab=score' },
      { label: '考试安排', desc: '考场 · 考试日历', mark: '考', path: 'academic/index?tab=exam' },
      { label: '公告通知', desc: '学校 · 院系通知', mark: '告', path: 'notice/index', key: 'notice' },
    ],
  },
  {
    name: '校园生活',
    desc: '缴费、校园卡与宿舍服务',
    tone: 'orange',
    items: [
      { label: '校园缴费', desc: '学费 · 电费充值', mark: '缴', path: 'life/index?tab=payment' },
      { label: '校园卡', desc: '余额 · 消费 · 挂失', mark: '卡', path: 'life/index?tab=card' },
      { label: '宿舍服务', desc: '成员 · 水电 · 公告', mark: '宿', path: 'life/index?tab=dorm' },
      { label: '校园报修', desc: '提交 · 进度 · 评价', mark: '修', path: 'repair/index' },
    ],
  },
  {
    name: '社区与出行',
    desc: '活动、交易与校园导航',
    tone: 'blue',
    items: [
      { label: '二手交易', desc: '闲置发布 · 下单', mark: '市', path: 'market/index' },
      { label: '校园活动', desc: '报名 · 签到', mark: '动', path: 'activity/index' },
      { label: '校园地图', desc: '定位 · 步行导航', mark: '图', path: 'map/index' },
      { label: '新闻资讯', desc: '校园新鲜事', mark: '闻', path: 'index/index', tab: true },
    ],
  },
]

const unread = ref(0)

function open(item) {
  if (item.tab) return uni.switchTab({ url: `/pages/${item.path}` })
  uni.navigateTo({ url: `/pages/${item.path}` })
}

onShow(() => {
  unread.value = 0
  if (uni.getStorageSync('token')) getUnreadNoticeCount().then((data) => { unread.value = Number(data?.count ?? data) || 0 }).catch(() => {})
})
</script>

<template>
  <view class="page">
    <view class="hero">
      <view class="hero-orb" />
      <text class="eyebrow">CAMPUS SERVICES</text>
      <text class="headline">全部校园服务</text>
      <text class="sub">按使用场景分类，快速找到你需要的功能</text>
    </view>

    <view v-for="group in groups" :key="group.name" class="group">
      <view class="group-head"><view><text class="group-title">{{ group.name }}</text><text class="group-desc">{{ group.desc }}</text></view><text class="group-count">{{ group.items.length }} 项</text></view>
      <view class="grid">
        <view v-for="item in group.items" :key="item.label" class="tile" @click="open(item)">
          <view class="mark" :class="group.tone">{{ item.mark }}<text v-if="item.key === 'notice' && unread" class="badge">{{ unread }}</text></view>
          <view class="tile-copy">
            <text class="tile-label">{{ item.label }}</text>
            <text class="tile-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page { min-height: 100vh; padding-bottom: 48rpx; }
.hero { position: relative; padding: 54rpx 30rpx 52rpx; overflow: hidden; background: linear-gradient(135deg, #241457 0%, #7352cc 58%, #b06fd0 100%); color: #fff; box-shadow: 0 18rpx 46rpx rgba(36,20,87,.22); }
.hero-orb { position: absolute; top: -100rpx; right: -50rpx; width: 300rpx; height: 300rpx; border-radius: 50%; background: rgba(255,255,255,.11); }
.eyebrow, .headline, .sub { display: block; }
.eyebrow { color: #ffe9ad; font-size: 21rpx; font-weight: 700; letter-spacing: 3rpx; }
.headline { margin-top: 10rpx; font-size: 42rpx; font-weight: 800; }
.sub { margin-top: 12rpx; color: #e2d8f7; font-size: 24rpx; }
.group { margin: 32rpx 24rpx 0; }
.group-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18rpx; color: $campus-text-main; }
.group-title,.group-desc { display: block; }.group-title { font-size: 30rpx; font-weight: 700; }.group-desc { margin-top: 5rpx; color: $campus-text-muted; font-size: 21rpx; }.group-count { padding: 7rpx 14rpx; border-radius: 999rpx; background: #eee8fa; color: #7657c7; font-size: 20rpx; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.tile { display: flex; align-items: center; gap: 18rpx; min-height: 132rpx; padding: 24rpx 22rpx; border: 1rpx solid #ece4fb; border-radius: 22rpx; background: rgba(255,255,255,.96); box-shadow: 0 8rpx 28rpx rgba(124,92,214,.08); }
.mark { position: relative; display: flex; flex: 0 0 68rpx; align-items: center; justify-content: center; width: 68rpx; height: 68rpx; border-radius: 19rpx; font-size: 28rpx; font-weight: 700; }
.mark.violet { background: #efe9fc; color: #694ab9; }.mark.orange { background: #fff0df; color: #c87528; }.mark.blue { background: #e8f1ff; color: #3976c9; }
.badge { position: absolute; top: -10rpx; right: -10rpx; min-width: 30rpx; height: 30rpx; padding: 0 7rpx; border-radius: 15rpx; background: #f56c6c; color: #fff; font-size: 19rpx; font-weight: 400; line-height: 30rpx; text-align: center; }
.tile-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; gap: 6rpx; }
.tile-label { font-size: 28rpx; font-weight: 600; color: $campus-text-main; }
.tile-desc { overflow: hidden; color: $campus-text-muted; font-size: 21rpx; white-space: nowrap; text-overflow: ellipsis; }
</style>
