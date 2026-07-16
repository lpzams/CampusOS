<script setup lang="ts">
/**
 * 根组件 —— 全站布局：顶栏（logo + 导航菜单 + 用户区）/ 内容区 / 页脚。
 *
 * 内容区的 <router-view /> 会根据当前 URL 渲染 router/index.ts 里配置的页面。
 *
 * 【新增功能时】若功能要出现在顶部导航，在下面 el-menu 里加一个
 * <el-menu-item index="/你的路由">菜单名</el-menu-item> 即可（index 就是路由 path）。
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

// 顶部菜单高亮逻辑：进详情页 /news/123 时也要让「新闻资讯」保持选中，
// 所以按路径前缀归类，而不是精确匹配整个 path。
const activeMenu = computed(() => {
  if (route.path.startsWith('/admin')) return '/admin/news'
  return '/news'
})
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">🏫 智慧校园门户</div>
      <!-- router 模式：点击菜单项即跳转到 index 对应的路由 -->
      <el-menu
        mode="horizontal"
        router
        :default-active="activeMenu"
        :ellipsis="false"
        class="menu"
      >
        <el-menu-item index="/news">新闻资讯</el-menu-item>
        <el-menu-item index="/admin/news">新闻管理</el-menu-item>
      </el-menu>
      <div class="user">你好，{{ userStore.name }}</div>
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
  gap: 24px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.menu {
  flex: 1;
  border-bottom: none;
}

.user {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.main {
  padding: 20px;
}

/* 内容限宽居中，大屏上不至于一行文字拉得太长 */
.page-container {
  max-width: 1000px;
  margin: 0 auto;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 13px;
}
</style>

<style>
/* 全局样式（不带 scoped）：清掉浏览器默认边距 */
body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
