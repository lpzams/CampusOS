<script setup lang="ts">
/**
 * 全站布局 —— 顶栏导航 + 内容区 + 页脚。
 *
 * 导航按模块分组：首页/资讯/通知是单项，教务/生活服务/校园生活是下拉分组，
 * 右侧是用户区（登录后显示头像+下拉，未登录显示登录/注册按钮）。
 * 用户下拉菜单整合自 CampusOS_a：个人中心 / 我的收藏 / 修改密码 / 退出登录。
 * immersive 路由（首页开机动画、登录/注册页）不套这套布局，直接整屏渲染。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Lock, Star, SwitchButton, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/auth'
import { fileUrl } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const immersive = computed(() => route.meta.immersive === true)
const widePage = computed(() => [
  '/course-selection', '/course-reviews', '/schedule', '/free-classrooms',
  '/score', '/score-statistics', '/exam', '/exam-calendar', '/teaching', '/map',
].includes(route.path))

/** 顶栏高亮：详情页归到所属列表项，其余按当前路径精确匹配 */
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/news/')) return '/news'
  if (path.startsWith('/admin/news')) return '/admin/news'
  if (path.startsWith('/notice/')) return '/notice'
  if (path.startsWith('/market/')) return '/market'
  if (path.startsWith('/activity/')) return '/activity'
  return path
})

async function handleLogout() {
  // 后端无状态，退出失败也要清本地登录态，所以放 finally
  try {
    await logoutApi()
  } catch {
    // 忽略：本地登出才是关键
  } finally {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

function handleUserCommand(command: string) {
  if (command === 'logout') handleLogout()
  else router.push(command)
}
</script>

<template>
  <router-view v-if="immersive" />

  <el-container v-else class="layout">
    <el-header class="header">
      <router-link to="/" class="logo">CampusOS</router-link>

      <el-menu mode="horizontal" router :default-active="activeMenu" :ellipsis="false" class="menu">
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/news">校园资讯</el-menu-item>
        <el-menu-item index="/notice">通知公告</el-menu-item>

        <el-sub-menu index="academic">
          <template #title>教务</template>
          <el-menu-item index="/schedule">{{ userStore.isTeacher ? '我的教学课表' : '我的课表' }}</el-menu-item>
          <el-menu-item v-if="!userStore.isTeacher" index="/course-selection">课程选择</el-menu-item>
          <el-menu-item v-if="!userStore.isTeacher" index="/course-reviews">课程评价</el-menu-item>
          <el-menu-item v-if="userStore.isTeacher" index="/teaching">教学管理</el-menu-item>
          <el-menu-item index="/free-classrooms">空闲教室</el-menu-item>
          <el-menu-item v-if="userStore.canAccess('/score')" index="/score">成绩查询</el-menu-item>
          <el-menu-item v-if="userStore.canAccess('/score-statistics')" index="/score-statistics">成绩统计</el-menu-item>
          <el-menu-item v-if="userStore.canAccess('/exam')" index="/exam">考试安排</el-menu-item>
          <el-menu-item v-if="userStore.canAccess('/exam-calendar')" index="/exam-calendar">考试日历</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.canAccess('/payment')" index="life">
          <template #title>生活服务</template>
          <el-menu-item index="/payment">校园缴费</el-menu-item>
          <el-menu-item index="/electricity-recharge">电费充值</el-menu-item>
          <el-menu-item index="/card">校园卡</el-menu-item>
          <el-menu-item index="/dormitory">宿舍管理</el-menu-item>
          <el-menu-item index="/utility">水电余额</el-menu-item>
          <el-menu-item index="/repair">校园报修</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="campus">
          <template #title>校园生活</template>
          <el-menu-item v-if="userStore.canAccess('/market')" index="/market">二手市场</el-menu-item>
          <el-menu-item index="/activity">校园活动</el-menu-item>
          <el-menu-item v-if="userStore.canAccess('/my-activities')" index="/my-activities">我的活动</el-menu-item>
          <el-menu-item index="/map">校园地图</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/assistant">AI 助手</el-menu-item>
        <el-sub-menu v-if="userStore.isAdmin" index="admin">
          <template #title>后台管理</template>
          <el-menu-item index="/admin/news">资讯管理</el-menu-item>
          <el-menu-item index="/admin/academic-publish">通知与考试发布</el-menu-item>
          <el-menu-item index="/admin/teaching">教学排课</el-menu-item>
          <el-menu-item index="/admin/repairs">报修工单管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="userStore.isAdmin" index="/admin/users">用户管理</el-menu-item>
      </el-menu>

      <!-- 用户区（下拉菜单整合自 CampusOS_a） -->
      <div class="user-area">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <span class="user-trigger">
              <el-avatar :size="30" :src="fileUrl(userStore.avatar)">{{ userStore.name.slice(0, 1) }}</el-avatar>
              <span class="user-name">{{ userStore.name }}</span>
              <el-icon class="user-caret"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="/favorites">
                  <el-icon><Star /></el-icon>我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="/change-password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <div class="page-container" :class="{ 'page-container--wide': widePage }"><router-view /></div>
    </el-main>

    <el-footer class="footer">CampusOS · 智慧校园门户</el-footer>
  </el-container>
</template>

<style scoped>
.layout { min-height: 100vh; background: linear-gradient(180deg, #f6f2fd 0%, #fbf3f9 60%, #fdf6f2 100%); }
.header { position: sticky; top: 0; z-index: 100; display: flex; align-items: center; gap: 16px; background: #ffffffd9; border-bottom: 1px solid #ece4fb; padding: 0 20px; backdrop-filter: blur(14px); }
.logo { color: #7c5cd6; font-size: 18px; font-weight: 800; text-decoration: none; white-space: nowrap; font-family: Georgia, serif; }
.menu { flex: 1; border-bottom: 0; background: transparent; }
.user-area { display: flex; align-items: center; gap: 8px; white-space: nowrap; }
.user-trigger { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; padding: 4px 10px; border-radius: 20px; transition: background-color .2s; }
.user-trigger:hover { background: #f4effd; }
.user-name { color: #606266; font-size: 14px; }
.user-caret { color: #a89ec9; font-size: 12px; }
.main { padding: 20px; }
.page-container { max-width: 1100px; margin: 0 auto; }
.page-container--wide { max-width: 1440px; }
.footer { display: flex; align-items: center; justify-content: center; color: #a89ec9; font-size: 13px; }

@media (max-width: 640px) {
  .header { gap: 8px; padding: 0 12px; }
  .logo { font-size: 15px; }
  .user-name { display: none; }
  .main { padding: 12px; }
}
</style>

<style>
* { box-sizing: border-box; }
html, body, #app { min-height: 100%; }
body { margin: 0; font-family: Inter, "PingFang SC", "Microsoft YaHei", sans-serif; }
button, input { font: inherit; }

/* 全站主题色：暮紫（与首页桌面的梦幻风格一致），Element Plus 组件自动跟随 */
:root {
  --el-color-primary: #7c5cd6;
  --el-color-primary-light-3: #a08ae3;
  --el-color-primary-light-5: #bfaeee;
  --el-color-primary-light-7: #dcd2f7;
  --el-color-primary-light-8: #e9e2fa;
  --el-color-primary-light-9: #f4effd;
  --el-color-primary-dark-2: #6247ab;
}

/* 卡片统一圆角柔和阴影，贴近桌面端的玻璃质感 */
.el-card {
  border-radius: 14px;
  border-color: #ece4fb;
  box-shadow: 0 6px 24px #7c5cd60f;
}
</style>
