<script setup lang="ts">
/**
 * 根组件 —— 全站布局：顶栏（logo + 导航菜单 + 用户区）/ 内容区 / 页脚。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  User,
  SwitchButton,
  ArrowDown,
  Lock,
  Star,
  HomeFilled,
  Document,
  Bell,
  Reading,
  DataAnalysis,
  Calendar,
  Wallet,
  CreditCard,
  HomeFilled as Dormitory,
  Tools,
  Shop,
  Tickets,
  MapLocation,
  Cpu,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// ===== 顶部菜单高亮逻辑（根据当前路由匹配父菜单） =====
const activeMenu = computed(() => {
  const path = route.path

  // 首页
  if (path === '/') return '/'

  // 新闻资讯相关
  if (path === '/news' || path.startsWith('/news/') || path.startsWith('/admin/news')) {
    return '/news'
  }

  // 公告通知
  if (path === '/notice') return '/notice'

  // 教学服务（课程/成绩/考试）
  if (['/schedule', '/today-courses', '/free-classrooms', '/scores', '/exam', '/exam-calendar'].includes(path)) {
    return '/teaching'
  }

  // 校园生活（缴费/校园卡/宿舍/报修）
  if (['/payments', '/payment-records', '/electricity-recharge', '/card', '/card/transactions', '/dormitory', '/dormitory/notice', '/dormitory/utility', '/repair', '/repair/submit', '/repair/detail'].includes(path) || path.startsWith('/repair/detail/')) {
    return '/life'
  }

  // 更多服务（二手/活动/地图/AI）
  if (['/products', '/product/publish', '/my-orders', '/activities', '/my-activities', '/locations', '/route-planner', '/ai-chat'].includes(path) || path.startsWith('/product/') || path.startsWith('/activity/') || path.startsWith('/location/')) {
    return '/more'
  }

  return '/'
})

// ===== 判断是否需要隐藏导航栏 =====
const hideNav = computed(() => route.meta.hideNav === true)

// ===== 默认头像 =====
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// ===== 退出登录 =====
async function handleLogout() {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    router.push('/login')
  }
}

// ===== 下拉菜单命令处理 =====
function handleDropdownCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'favorites') {
    router.push('/favorites')
  } else if (command === 'change-password') {
    router.push('/change-password')
  } else if (command === 'logout') {
    handleLogout()
  }
}
</script>

<template>
  <!-- ====== 登录/注册/忘记密码页：纯内容 ====== -->
  <div v-if="hideNav" id="app">
    <router-view />
  </div>

  <!-- ====== 其他页面：完整布局 ====== -->
  <el-container v-else class="layout">
    <el-header class="header">
      <!-- Logo -->
      <div class="logo">🏫 智慧校园门户</div>

      <!-- ===== 导航菜单（优化后：仅 6 个一级项，含 3 个下拉菜单） ===== -->
      <el-menu
        mode="horizontal"
        router
        :default-active="activeMenu"
        :ellipsis="false"
        class="menu"
      >
        <!-- 首页 -->
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 新闻资讯 -->
        <el-menu-item index="/news">
          <el-icon><Document /></el-icon>
          <span>新闻资讯</span>
        </el-menu-item>

        <!-- 公告通知 -->
        <el-menu-item index="/notice">
          <el-icon><Bell /></el-icon>
          <span>公告通知</span>
        </el-menu-item>

        <!-- ===== 教学服务（下拉） ===== -->
        <el-sub-menu index="/teaching">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>教学服务</span>
          </template>
          <el-menu-item index="/schedule">我的课表</el-menu-item>
          <el-menu-item index="/today-courses">今日课程</el-menu-item>
          <el-menu-item index="/free-classrooms">空闲教室</el-menu-item>
          <el-menu-item index="/scores">成绩查询</el-menu-item>
          <el-menu-item index="/exam">考试安排</el-menu-item>
          <el-menu-item index="/exam-calendar">考试日历</el-menu-item>
        </el-sub-menu>

        <!-- ===== 校园生活（下拉） ===== -->
        <el-sub-menu index="/life">
          <template #title>
            <el-icon><Dormitory /></el-icon>
            <span>校园生活</span>
          </template>
          <el-menu-item index="/payments">待缴费</el-menu-item>
          <el-menu-item index="/payment-records">缴费记录</el-menu-item>
          <el-menu-item index="/electricity-recharge">电费充值</el-menu-item>
          <el-menu-item index="/card">校园卡</el-menu-item>
          <el-menu-item index="/card/transactions">消费记录</el-menu-item>
          <el-menu-item index="/dormitory">宿舍信息</el-menu-item>
          <el-menu-item index="/dormitory/notice">宿舍公告</el-menu-item>
          <el-menu-item index="/dormitory/utility">水电余额</el-menu-item>
          <el-menu-item index="/repair">报修记录</el-menu-item>
          <el-menu-item index="/repair/submit">提交报修</el-menu-item>
        </el-sub-menu>

        <!-- ===== 更多服务（下拉） ===== -->
        <el-sub-menu index="/more">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>更多服务</span>
          </template>
          <el-menu-item index="/products">二手交易</el-menu-item>
          <el-menu-item index="/product/publish">发布商品</el-menu-item>
          <el-menu-item index="/my-orders">我的订单</el-menu-item>
          <el-menu-item index="/activities">校园活动</el-menu-item>
          <el-menu-item index="/my-activities">我的活动</el-menu-item>
          <el-menu-item index="/locations">校园地图</el-menu-item>
          <el-menu-item index="/route-planner">路径规划</el-menu-item>
          <el-menu-item index="/ai-chat">AI助手</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <!-- ===== 用户区域（下拉菜单） ===== -->
      <div class="user-area">
        <el-dropdown trigger="click" @command="handleDropdownCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userStore.avatar || defaultAvatar">
              {{ userStore.name?.[0] }}
            </el-avatar>
            <span class="user-name">{{ userStore.name }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="favorites">
                <el-icon><Star /></el-icon>我的收藏
              </el-dropdown-item>
              <el-dropdown-item command="change-password">
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main">
      <div class="page-container">
        <router-view />
      </div>
    </el-main>

    <el-footer class="footer">
      CampusOS · 高校智慧校园门户系统 · 课程实践项目
    </el-footer>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background-color: #f5f6fa;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 16px;
  height: 60px;
  flex-shrink: 0;
}

.logo {
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.menu {
  flex: 1;
  border-bottom: none;
  min-width: 0;
  overflow: hidden;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  font-size: 14px;
  padding: 0 12px;
}

/* ✅ 修复下拉菜单箭头位置（让箭头和文字分开） */
.menu :deep(.el-sub-menu__icon-arrow) {
  position: static;
  margin-left: 6px;
  font-size: 12px;
  color: #909399;
}

/* ===== 用户区域 ===== */
.user-area {
  display: flex;
  align-items: center;
  white-space: nowrap;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  transition: background-color 0.2s;
}

.user-dropdown:hover {
  background-color: #f0f2f5;
}

.user-name {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.el-icon--right {
  color: #909399;
  font-size: 12px;
}

.main {
  padding: 20px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 13px;
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  height: 50px;
  flex-shrink: 0;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .header {
    gap: 8px;
    padding: 0 8px;
  }
  .logo {
    font-size: 14px;
  }
  .menu :deep(.el-menu-item),
  .menu :deep(.el-sub-menu__title) {
    font-size: 13px;
    padding: 0 8px;
  }
  .user-name {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .header {
    flex-wrap: nowrap;
    overflow-x: auto;
    gap: 4px;
  }
  .logo {
    font-size: 14px;
    flex-shrink: 0;
  }
  .menu {
    flex: 1;
    min-width: 0;
  }
  .menu :deep(.el-menu-item),
  .menu :deep(.el-sub-menu__title) {
    font-size: 12px;
    padding: 0 6px;
  }
  .user-name {
    display: none;
  }
  .user-dropdown {
    padding: 4px 6px;
  }
}
</style>

<style>
/* 全局样式 */
body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}
</style>