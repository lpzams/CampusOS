/**
 * 路由表 —— 页面地址和组件的对应关系，全项目只有这一份。
 *
 * 组件用 () => import(...) 懒加载：访问到该页面才下载对应代码，首屏更快。
 *
 * 【新增功能时】两步：
 *  1. 在 views/你的功能/ 下写页面组件；
 *  2. 在下面 routes 数组里加一条 { path, component, meta.title }。
 * 需要出现在顶部导航的话，再去 App.vue 的 el-menu 里加一个 menu-item。
 */
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  // history 模式（URL 里没有 #）。部署到 nginx 时需要配 try_files 回退到 index.html
  history: createWebHistory(),
  routes: [
    // 首页暂时直接跳新闻列表；以后做了门户首页（功能聚合页）再换
    { path: '/', redirect: '/news' },
    {
      path: '/news',
      name: 'news-list',
      component: () => import('@/views/news/NewsListView.vue'),
      meta: { title: '新闻资讯' },
    },
    {
      path: '/news/:id',
      name: 'news-detail',
      component: () => import('@/views/news/NewsDetailView.vue'),
      meta: { title: '新闻详情' },
    },
    {
      path: '/admin/news',
      name: 'admin-news',
      component: () => import('@/views/admin/NewsManageView.vue'),
      meta: { title: '新闻管理' },
    },
  ],
})

// 每次切换路由后，把浏览器标签页标题同步成当前页面的标题
router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - 智慧校园门户` : '智慧校园门户'
})

export default router
