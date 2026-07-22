/**
 * 路由表 —— 加页面 = 加一条路由。
 *
 * 【meta 约定】
 *   title：浏览器标题（afterEach 里统一设置）；
 *   immersive：true 表示全屏沉浸式页面（不套 App.vue 的顶栏/页脚），如首页开机动画、登录页；
 *   requiresAuth：true 表示需要登录才能访问，未登录会被守卫重定向到 /login?redirect=原路径。
 *
 * 【登录守卫】见文件底部 beforeEach：只挡「页面级」访问；
 * 具体接口的 401 由 utils/request.ts 兜底处理（token 过期等）。
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ===== 沉浸式页面（不套全站布局） =====
    {
      path: '/',
      name: 'campus-os',
      component: () => import('@/views/home/CampusOsView.vue'),
      meta: { title: 'CampusOS', immersive: true },
    },
    {
      path: '/login',
      name: 'login',
      redirect: to => ({ path: '/', query: { ...to.query, login: '1' } }),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { title: '注册', immersive: true },
    },

    // ===== 门户首页（整合自 CampusOS_a：用户卡 + 快捷导航宫格 + 资讯速览） =====
    { path: '/home', name: 'portal-home', component: () => import('@/views/home/PortalHomeView.vue'), meta: { title: '门户首页', requiresAuth: true } },

    // ===== 新闻资讯（功能 3） =====
    { path: '/news', name: 'news-list', component: () => import('@/views/news/NewsListView.vue'), meta: { title: '校园资讯' } },
    { path: '/news/:id', name: 'news-detail', component: () => import('@/views/news/NewsDetailView.vue'), meta: { title: '资讯详情' } },
    { path: '/admin/news', name: 'admin-news', component: () => import('@/views/admin/NewsManageView.vue'), meta: { title: '资讯管理', requiresAuth: true } },
    { path: '/admin/academic-publish', name: 'admin-academic-publish', component: () => import('@/views/admin/AcademicPublishView.vue'), meta: { title: '通知与考试发布', requiresAuth: true } },
    { path: '/admin/teaching', name: 'admin-teaching', component: () => import('@/views/admin/TeachingArrangeView.vue'), meta: { title: '教学排课', requiresAuth: true } },
    { path: '/admin/repairs', name: 'admin-repairs', component: () => import('@/views/admin/RepairManageView.vue'), meta: { title: '报修工单管理', requiresAuth: true } },
    { path: '/admin/users', name: 'admin-users', component: () => import('@/views/admin/UserManageView.vue'), meta: { title: '用户与角色管理', requiresAuth: true } },
    { path: '/favorites', name: 'favorites', component: () => import('@/views/news/FavoriteView.vue'), meta: { title: '我的收藏', requiresAuth: true } },

    // ===== 通知公告（功能 4） =====
    { path: '/notice', name: 'notice-list', component: () => import('@/views/notice/NoticeListView.vue'), meta: { title: '通知公告' } },
    { path: '/notice/:id', name: 'notice-detail', component: () => import('@/views/notice/NoticeDetailView.vue'), meta: { title: '公告详情' } },

    // ===== 个人中心（功能 2） =====
    { path: '/profile', name: 'profile', component: () => import('@/views/profile/ProfileView.vue'), meta: { title: '个人中心', requiresAuth: true } },
    { path: '/change-password', name: 'change-password', component: () => import('@/views/profile/ChangePasswordView.vue'), meta: { title: '修改密码', requiresAuth: true } },

    // ===== 教务（功能 5/6/7） =====
    { path: '/schedule', name: 'schedule', component: () => import('@/views/academic/ScheduleView.vue'), meta: { title: '我的课表', requiresAuth: true } },
    { path: '/course-selection', name: 'course-selection', component: () => import('@/views/academic/CourseSelectionView.vue'), meta: { title: '课程选择', requiresAuth: true } },
    { path: '/course-reviews', name: 'course-reviews', component: () => import('@/views/academic/CourseReviewView.vue'), meta: { title: '课程评价', requiresAuth: true } },
    { path: '/teaching', name: 'teaching', component: () => import('@/views/academic/TeachingManageView.vue'), meta: { title: '教学管理', requiresAuth: true } },
    { path: '/free-classrooms', name: 'free-classrooms', component: () => import('@/views/academic/FreeClassroomsView.vue'), meta: { title: '空闲教室', requiresAuth: true } },
    { path: '/score', name: 'score', component: () => import('@/views/academic/ScoreView.vue'), meta: { title: '成绩查询', requiresAuth: true } },
    { path: '/score-statistics', name: 'score-statistics', component: () => import('@/views/academic/ScoreStatsView.vue'), meta: { title: '成绩统计分析', requiresAuth: true } },
    { path: '/exam', name: 'exam', component: () => import('@/views/academic/ExamView.vue'), meta: { title: '考试安排', requiresAuth: true } },
    { path: '/exam-calendar', name: 'exam-calendar', component: () => import('@/views/academic/ExamCalendarView.vue'), meta: { title: '考试日历', requiresAuth: true } },

    // ===== 生活服务（功能 8/9/10/11） =====
    { path: '/payment', name: 'payment', component: () => import('@/views/life/PaymentView.vue'), meta: { title: '校园缴费', requiresAuth: true } },
    { path: '/electricity-recharge', name: 'electricity-recharge', component: () => import('@/views/life/ElectricityRechargeView.vue'), meta: { title: '电费充值', requiresAuth: true } },
    { path: '/card', name: 'card', component: () => import('@/views/life/CardView.vue'), meta: { title: '校园卡', requiresAuth: true } },
    { path: '/dormitory', name: 'dormitory', component: () => import('@/views/life/DormitoryView.vue'), meta: { title: '宿舍管理', requiresAuth: true } },
    { path: '/utility', name: 'utility', component: () => import('@/views/life/UtilityView.vue'), meta: { title: '水电余额', requiresAuth: true } },
    { path: '/repair', name: 'repair', component: () => import('@/views/life/RepairView.vue'), meta: { title: '校园报修', requiresAuth: true } },

    // ===== 校园生活（功能 12/13/14） =====
    { path: '/market', name: 'market-list', component: () => import('@/views/market/ProductListView.vue'), meta: { title: '二手市场' } },
    { path: '/market/:id', name: 'market-detail', component: () => import('@/views/market/ProductDetailView.vue'), meta: { title: '商品详情' } },
    { path: '/activity', name: 'activity-list', component: () => import('@/views/activity/ActivityListView.vue'), meta: { title: '校园活动' } },
    { path: '/activity/:id', name: 'activity-detail', component: () => import('@/views/activity/ActivityDetailView.vue'), meta: { title: '活动详情' } },
    { path: '/my-activities', name: 'my-activities', component: () => import('@/views/activity/MyActivitiesView.vue'), meta: { title: '我的活动', requiresAuth: true } },
    { path: '/map', name: 'map', component: () => import('@/views/map/MapView.vue'), meta: { title: '校园地图' } },

    // ===== AI 助手（功能 15） =====
    { path: '/assistant', name: 'assistant', component: () => import('@/views/assistant/AssistantView.vue'), meta: { title: 'AI 助手' } },

    // 兜底：未匹配的路径回首页
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

// 未登录只能看开机锁屏和注册页，所有门户模块统一从 Windows 锁屏登录。
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.path !== '/' && to.path !== '/login' && to.path !== '/register' && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (userStore.isLoggedIn && !userStore.canAccess(to.path)) {
    return { path: '/home' }
  }
  return true
})

router.afterEach((to) => {
  document.title = `${String(to.meta.title ?? 'CampusOS')} - 智慧校园`
})

export default router
