<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { assetUrl } from '@/utils/request'
import { changePassword, getFavoriteNews, getProfile, getStudentProfile, getTeacherProfile, logout, publishNews, refreshToken, updateProfile, uploadAvatar, verifyIdentity } from '@/api/campus'

const profile = ref(null)
const activeSection = ref('profile')
const edit = reactive({ realName: '', phone: '', email: '', gender: '' })
const password = reactive({ oldPassword: '', newPassword: '' })
const verify = reactive({ realName: '', idCard: '', studentId: '' })
const favorites = ref([])
const article = reactive({ title: '', content: '', coverImage: '', categoryId: 1, summary: '', isTop: false, isPublished: true })

// 学籍信息（学生）/ 教师档案：后端用户数据是动态 Map，字段随注册信息而定，只展示填过的那几项。
const archiveTitle = computed(() => profile.value?.userType === 2 ? '教师档案' : '学籍信息')
const archive = computed(() => {
  const p = profile.value
  if (!p || (p.userType !== 1 && p.userType !== 2)) return []
  const pick = (label, key) => ({ label, value: p[key] })
  const rows = p.userType === 2
    ? [pick('工号', 'studentId'), pick('院系', 'department'), pick('职称', 'title'), pick('研究方向', p.researchField != null ? 'researchField' : 'researchDirection')]
    : [pick('学号', 'studentId'), pick('院系', 'department'), pick('专业', 'major'), pick('班级', 'className'), pick('入学年份', 'enrollmentYear'), pick('宿舍', 'dormitory'), pick('辅导员', 'advisor')]
  return rows.filter((row) => row.value !== undefined && row.value !== null && row.value !== '')
})

async function load() {
  // 「我的」是 tabBar 页，不能像普通页那样 onLoad 里 redirect；未登录时展示登录引导。
  if (!uni.getStorageSync('token')) { profile.value = null; return }
  const base = await getProfile()
  const detail = base.userType === 1 ? await getStudentProfile().catch(() => ({})) : base.userType === 2 ? await getTeacherProfile().catch(() => ({})) : {}
  profile.value = { ...base, ...detail }
  Object.assign(edit, profile.value)
  verify.realName = profile.value.realName || ''
  verify.studentId = profile.value.studentId || ''
  favorites.value = await getFavoriteNews().catch(() => [])
}

function toLogin() { uni.navigateTo({ url: '/pages/auth/index' }) }
async function save() { profile.value = await updateProfile(edit); uni.showToast({ title: '已保存' }) }
function chooseAvatar() {
  uni.chooseImage({ count: 1, sizeType: ['compressed'], success: async ({ tempFilePaths }) => { profile.value = await uploadAvatar(tempFilePaths[0]); uni.showToast({ title: '头像已更新' }) } })
}
async function changePwd() { await changePassword(password); password.oldPassword = ''; password.newPassword = ''; uni.showToast({ title: '密码已修改' }) }
async function submitVerify() { await verifyIdentity(verify); uni.showToast({ title: '已提交审核' }) }
async function refresh() { const data = await refreshToken(); uni.setStorageSync('token', data.token); uni.showToast({ title: '登录已刷新' }) }
async function publish() { await publishNews(article); uni.showToast({ title: '新闻已发布' }); article.title = ''; article.content = ''; article.summary = '' }
async function signOut() { await logout().catch(() => {}); uni.clearStorageSync(); profile.value = null; uni.switchTab({ url: '/pages/index/index' }) }

onShow(load)

function toggleSection(name) { activeSection.value = activeSection.value === name ? '' : name }
</script>

<template>
  <view v-if="profile" class="screen">
    <view class="profile-hero">
      <view class="hero-caption">PERSONAL SPACE</view>
      <image v-if="profile.avatar" class="avatar" :src="assetUrl(profile.avatar)" mode="aspectFill" @click="chooseAvatar" />
      <view v-else class="avatar fallback" @click="chooseAvatar">{{ (profile.realName || profile.username || '我').slice(0, 1) }}</view>
      <view class="identity-copy"><view class="name">{{ profile.realName || profile.username }}</view><view class="identity-meta">{{ profile.department || '暂未填写院系' }} · {{ profile.userType === 2 ? '教师' : profile.userType === 3 ? '管理员' : '学生' }}</view></view>
      <view class="avatar-tip">点击更换头像</view>
    </view>
    <view v-if="archive.length" class="panel archive-panel"><view class="section-title">{{ archiveTitle }}</view><view v-for="row in archive" :key="row.label" class="archive-row"><text class="muted">{{ row.label }}</text><text class="archive-value">{{ row.value }}</text></view></view>
    <view class="setting-list">
      <view class="setting-card" @click="toggleSection('profile')"><view class="setting-icon violet">✦</view><view class="setting-copy"><text class="setting-title">个人资料</text><text class="setting-desc">姓名、联系方式与基本信息</text></view><text class="chevron" :class="{ open: activeSection === 'profile' }">⌄</text></view>
      <view v-if="activeSection === 'profile'" class="setting-body"><input v-model="edit.realName" class="field" placeholder="姓名" /><input v-model="edit.phone" class="field" type="number" placeholder="手机号" /><input v-model="edit.email" class="field" placeholder="邮箱" /><input v-model="edit.gender" class="field" placeholder="性别" /><button class="primary-btn" @click="save">保存资料</button></view>
      <view class="setting-card" @click="toggleSection('verify')"><view class="setting-icon blue">✓</view><view class="setting-copy"><text class="setting-title">实名认证</text><text class="setting-desc">认证后可使用更多校园服务</text></view><text class="chevron" :class="{ open: activeSection === 'verify' }">⌄</text></view>
      <view v-if="activeSection === 'verify'" class="setting-body"><input v-model="verify.realName" class="field" placeholder="真实姓名" /><input v-model="verify.idCard" class="field" placeholder="身份证号" /><input v-model="verify.studentId" class="field" placeholder="学号 / 工号" /><button class="secondary-btn" @click="submitVerify">提交认证</button></view>
      <view class="setting-card" @click="toggleSection('security')"><view class="setting-icon orange">⌘</view><view class="setting-copy"><text class="setting-title">账号安全</text><text class="setting-desc">修改密码与刷新登录状态</text></view><text class="chevron" :class="{ open: activeSection === 'security' }">⌄</text></view>
      <view v-if="activeSection === 'security'" class="setting-body"><input v-model="password.oldPassword" class="field" password placeholder="原密码" /><input v-model="password.newPassword" class="field" password placeholder="新密码" /><button class="primary-btn" @click="changePwd">修改密码</button><button class="secondary-btn extra" @click="refresh">刷新登录状态</button></view>
      <view class="setting-card" @click="toggleSection('favorites')"><view class="setting-icon pink">♡</view><view class="setting-copy"><text class="setting-title">我的新闻收藏</text><text class="setting-desc">{{ favorites.length ? `${favorites.length} 篇收藏` : '还没有收藏内容' }}</text></view><text class="chevron" :class="{ open: activeSection === 'favorites' }">⌄</text></view>
      <view v-if="activeSection === 'favorites'" class="setting-body favorite-body"><view v-for="item in favorites" :key="item.id" class="favorite-row" @click="uni.navigateTo({url:`/pages/news/detail?id=${item.id}`})">{{ item.title }}<text>›</text></view><view v-if="!favorites.length" class="muted">暂无收藏</view></view>
      <view v-if="profile.userType === 3" class="setting-card" @click="toggleSection('publish')"><view class="setting-icon green">＋</view><view class="setting-copy"><text class="setting-title">发布新闻</text><text class="setting-desc">管理员内容发布工具</text></view><text class="chevron" :class="{ open: activeSection === 'publish' }">⌄</text></view>
      <view v-if="profile.userType === 3 && activeSection === 'publish'" class="setting-body"><input v-model="article.title" class="field" placeholder="新闻标题"/><input v-model.number="article.categoryId" class="field" type="number" placeholder="分类 ID"/><input v-model="article.coverImage" class="field" placeholder="封面图 URL（可选）"/><input v-model="article.summary" class="field" placeholder="摘要"/><textarea v-model="article.content" class="textarea" placeholder="新闻内容（支持 HTML）"/><button class="primary-btn" @click="publish">发布新闻</button></view>
    </view>
    <button class="danger-btn signout" @click="signOut">退出登录</button>
  </view>
  <view v-else class="screen guest">
    <view class="guest-card">
      <view class="guest-avatar">我</view>
      <view class="guest-title">未登录</view>
      <view class="muted">登录后查看学籍档案、成绩、缴费与更多校园服务</view>
      <button class="primary-btn guest-btn" @click="toLogin">登录 / 注册</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.profile-hero { position: relative; display: flex; align-items: center; min-height: 224rpx; margin-bottom: 24rpx; padding: 62rpx 30rpx 30rpx; overflow: hidden; border-radius: 26rpx; background: linear-gradient(135deg, #241457, #7c5cd6 65%, #b06fd0); color: #fff; box-shadow: 0 16rpx 40rpx rgba(36,20,87,.22); }
.profile-hero::after { position: absolute; right: -80rpx; bottom: -150rpx; width: 300rpx; height: 300rpx; border-radius: 50%; background: rgba(255,255,255,.1); content: ''; }
.hero-caption { position: absolute; top: 24rpx; left: 30rpx; color: #ffe9ad; font-size: 20rpx; font-weight: 700; letter-spacing: 3rpx; }
.avatar { width: 112rpx; height: 112rpx; border: 4rpx solid rgba(255,255,255,.5); border-radius: 56rpx; background: #f0f9f6; }
.fallback { display: flex; align-items: center; justify-content: center; color: #694ab9; font-size: 38rpx; font-weight: 700; }
.identity-copy { margin-left: 24rpx; }.avatar-tip { position: absolute; right: 28rpx; bottom: 24rpx; color: rgba(255,255,255,.65); font-size: 20rpx; }
.name { margin-bottom: 8rpx; font-size: 34rpx; font-weight: 700; }
.identity-meta { color: rgba(255,255,255,.72); font-size: 23rpx; }
.archive-panel { padding-bottom: 16rpx; }.setting-card { display: flex; align-items: center; min-height: 112rpx; margin-bottom: 2rpx; padding: 20rpx 18rpx; border-bottom: 1rpx solid #eee9f7; background: rgba(255,255,255,.88); }.setting-card:first-child { border-radius: 24rpx 24rpx 0 0; }.setting-card:last-child { border-radius: 0 0 24rpx 24rpx; }.setting-list { overflow: hidden; margin-bottom: 26rpx; border: 1rpx solid #ece4fb; border-radius: 24rpx; background: rgba(255,255,255,.8); box-shadow: 0 10rpx 30rpx rgba(124,92,214,.06); }.setting-icon { display: flex; align-items: center; justify-content: center; width: 62rpx; height: 62rpx; margin-right: 18rpx; border-radius: 18rpx; font-size: 30rpx; font-weight: 700; }.setting-icon.violet { background:#efe9fc;color:#694ab9 }.setting-icon.blue { background:#e8f1ff;color:#3976c9 }.setting-icon.orange { background:#fff0df;color:#c87528 }.setting-icon.pink { background:#faeaf3;color:#b95387 }.setting-icon.green { background:#e6f7f1;color:#288469 }.setting-copy { flex:1; }.setting-title,.setting-desc { display:block; }.setting-title { color:$campus-text-main;font-size:28rpx;font-weight:600 }.setting-desc { margin-top:5rpx;color:$campus-text-muted;font-size:21rpx }.chevron { color:#a49abd;font-size:34rpx;transform:rotate(-90deg);transition:transform .2s }.chevron.open { transform:rotate(0) }.setting-body { padding:26rpx 24rpx 28rpx; background:#fbfaff; }.favorite-body { padding-top:8rpx; }.signout { margin-top: 8rpx; }
.extra { margin-top: 16rpx; }
.favorite-row { display:flex;justify-content:space-between;padding:18rpx 0;border-bottom:1rpx solid #ebeef5;color:$campus-text-main;line-height:1.5 }
.archive-row { display: flex; align-items: center; justify-content: space-between; padding: 16rpx 0; gap: 24rpx; border-bottom: 1rpx solid #ebeef5; }
.archive-row:last-child { border-bottom: 0; }
.archive-value { flex: 1; color: $campus-text-main; text-align: right; }
.guest { display: flex; align-items: center; justify-content: center; min-height: 70vh; }
.guest-card { display: flex; align-items: center; flex-direction: column; width: 100%; padding: 72rpx 40rpx; border: 1rpx solid #ece4fb; border-radius: 26rpx; background: #fff; box-shadow: 0 12rpx 36rpx rgba(79,54,145,.08); }
.guest-avatar { display: flex; align-items: center; justify-content: center; width: 128rpx; height: 128rpx; border-radius: 64rpx; background: #efe9fc; color: #694ab9; font-size: 44rpx; font-weight: 700; }
.guest-title { margin: 26rpx 0 12rpx; font-size: 34rpx; font-weight: 700; }
.guest .muted { text-align: center; }
.guest-btn { width: 100%; margin-top: 40rpx; }
</style>
