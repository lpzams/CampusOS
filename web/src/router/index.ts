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
// ✅ 修复：使用 @ 别名
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ============================================================
    // ===== 认证模块（不需要登录） =====
    // ============================================================
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false, hideNav: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/RegisterView.vue'),
      meta: { title: '注册', requiresAuth: false, hideNav: true },
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/views/forgot/ForgotPasswordView.vue'),
      meta: { title: '重置密码', requiresAuth: false, hideNav: true },
    },

    // ============================================================
    // ===== 用户中心（需要登录） =====
    // ============================================================
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/profile/ProfileView.vue'),
      meta: { title: '个人信息', requiresAuth: true },
    },
    {
      path: '/profile/edit',
      name: 'profile-edit',
      component: () => import('@/views/profile/ProfileEditView.vue'),
      meta: { title: '编辑个人信息', requiresAuth: true },
    },
    {
      path: '/verify',
      name: 'verify',
      component: () => import('@/views/verify/VerifyView.vue'),
      meta: { title: '实名认证', requiresAuth: true },
    },
    {
      path: '/change-password',
      name: 'change-password',
      component: () => import('@/views/password/ChangePasswordView.vue'),
      meta: { title: '修改密码', requiresAuth: true },
    },

    // ============================================================
    // ===== 新闻资讯（列表和详情无需登录，管理需要登录） =====
    // ============================================================
    {
      path: '/news',
      name: 'news-list',
      component: () => import('@/views/news/NewsListView.vue'),
      meta: { title: '新闻资讯', requiresAuth: false },
    },
    {
      path: '/news/:id',
      name: 'news-detail',
      component: () => import('@/views/news/NewsDetailView.vue'),
      meta: { title: '新闻详情', requiresAuth: false },
    },
    {
      path: '/admin/news',
      name: 'admin-news',
      component: () => import('@/views/admin/NewsManageView.vue'),
      meta: { title: '新闻管理', requiresAuth: true },
    },
    {
      path: '/admin/news/publish',
      name: 'admin-news-publish',
      component: () => import('@/views/admin/NewsPublishView.vue'),
      meta: { title: '发布新闻', requiresAuth: true },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/news/FavoriteView.vue'),
      meta: { title: '我的收藏', requiresAuth: true },
    },

    // ============================================================
    // ===== 公告通知 =====
    // ============================================================
    {
      path: '/notice',
      name: 'notice-list',
      component: () => import('@/views/notice/NoticeListView.vue'),
      meta: { title: '公告通知', requiresAuth: false },
    },
    {
      path: '/notice/:id',
      name: 'notice-detail',
      component: () => import('@/views/notice/NoticeDetailView.vue'),
      meta: { title: '公告详情', requiresAuth: false },
    },

    // ============================================================
    // ===== 课程管理 =====
    // ============================================================
    {
      path: '/schedule',
      name: 'schedule',
      component: () => import('@/views/course/ScheduleView.vue'),
      meta: { title: '我的课表', requiresAuth: true },
    },
    {
      path: '/today-courses',
      name: 'today-courses',
      component: () => import('@/views/course/TodayCoursesView.vue'),
      meta: { title: '今日课程', requiresAuth: true },
    },
    {
      path: '/free-classrooms',
      name: 'free-classrooms',
      component: () => import('@/views/course/FreeClassroomsView.vue'),
      meta: { title: '空闲教室', requiresAuth: false },
    },

    // ============================================================
    // ===== 成绩查询 =====
    // ============================================================
    {
      path: '/scores',
      name: 'scores',
      component: () => import('@/views/score/ScoreListView.vue'),
      meta: { title: '成绩查询', requiresAuth: true },
    },
    {
      path: '/gpa',
      name: 'gpa',
      component: () => import('@/views/score/GPADashboard.vue'),
      meta: { title: 'GPA仪表盘', requiresAuth: true },
    },
    {
      path: '/score-statistics',
      name: 'score-statistics',
      component: () => import('@/views/score/ScoreStatistics.vue'),
      meta: { title: '成绩统计分析', requiresAuth: true },
    },

    // ============================================================
    // ===== 考试安排 =====
    // ============================================================
    {
      path: '/exam',
      name: 'exam',
      component: () => import('@/views/exam/ExamListView.vue'),
      meta: { title: '考试安排', requiresAuth: true },
    },
    {
      path: '/exam-calendar',
      name: 'exam-calendar',
      component: () => import('@/views/exam/ExamCalendarView.vue'),
      meta: { title: '考试日历', requiresAuth: true },
    },

    // ============================================================
    // ===== 校园缴费 =====
    // ============================================================
    {
      path: '/payments',
      name: 'payments',
      component: () => import('@/views/payment/PendingPaymentsView.vue'),
      meta: { title: '校园缴费', requiresAuth: true },
    },
    {
      path: '/payment-records',
      name: 'payment-records',
      component: () => import('@/views/payment/PaymentRecordsView.vue'),
      meta: { title: '缴费记录', requiresAuth: true },
    },
    {
      path: '/electricity-recharge',
      name: 'electricity-recharge',
      component: () => import('@/views/payment/ElectricityRechargeView.vue'),
      meta: { title: '电费充值', requiresAuth: true },
    },

    // ============================================================
    // ===== 校园卡 =====
    // ============================================================
    {
      path: '/card',
      name: 'card',
      component: () => import('@/views/card/CardInfoView.vue'),
      meta: { title: '校园卡', requiresAuth: true },
    },
    {
      path: '/card/transactions',
      name: 'card-transactions',
      component: () => import('@/views/card/CardTransactionsView.vue'),
      meta: { title: '消费记录', requiresAuth: true },
    },

    // ============================================================
    // ===== 宿舍管理 =====
    // ============================================================
    {
      path: '/dormitory',
      name: 'dormitory',
      component: () => import('@/views/dormitory/DormitoryInfoView.vue'),
      meta: { title: '宿舍信息', requiresAuth: true },
    },
    {
      path: '/dormitory/notice',
      name: 'dormitory-notice',
      component: () => import('@/views/dormitory/DormitoryNoticeView.vue'),
      meta: { title: '宿舍公告', requiresAuth: false },
    },
    {
      path: '/dormitory/utility',
      name: 'dormitory-utility',
      component: () => import('@/views/dormitory/UtilityView.vue'),
      meta: { title: '水电余额', requiresAuth: true },
    },

    // ============================================================
    // ===== 校园报修 =====
    // ============================================================
    {
      path: '/repair',
      name: 'repair',
      component: () => import('@/views/repair/RepairListView.vue'),
      meta: { title: '报修记录', requiresAuth: true },
    },
    {
      path: '/repair/submit',
      name: 'repair-submit',
      component: () => import('@/views/repair/SubmitRepairView.vue'),
      meta: { title: '提交报修', requiresAuth: true },
    },
    {
      path: '/repair/detail/:id',
      name: 'repair-detail',
      component: () => import('@/views/repair/RepairDetailView.vue'),
      meta: { title: '报修详情', requiresAuth: true },
    },

    // ============================================================
    // ===== 二手交易 =====
    // ============================================================
    {
      path: '/products',
      name: 'products',
      component: () => import('@/views/product/ProductListView.vue'),
      meta: { title: '二手交易', requiresAuth: false },
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: () => import('@/views/product/ProductDetailView.vue'),
      meta: { title: '商品详情', requiresAuth: false },
    },
    {
      path: '/product/publish',
      name: 'product-publish',
      component: () => import('@/views/product/PublishProductView.vue'),
      meta: { title: '发布商品', requiresAuth: true },
    },
    {
      path: '/my-orders',
      name: 'my-orders',
      component: () => import('@/views/product/MyOrdersView.vue'),
      meta: { title: '我的订单', requiresAuth: true },
    },

    // ============================================================
    // ===== 校园活动 =====
    // ============================================================
    {
      path: '/activities',
      name: 'activities',
      component: () => import('@/views/activity/ActivityListView.vue'),
      meta: { title: '校园活动', requiresAuth: false },
    },
    {
      path: '/activity/:id',
      name: 'activity-detail',
      component: () => import('@/views/activity/ActivityDetailView.vue'),
      meta: { title: '活动详情', requiresAuth: false },
    },
    {
      path: '/my-activities',
      name: 'my-activities',
      component: () => import('@/views/activity/MyActivitiesView.vue'),
      meta: { title: '我的活动', requiresAuth: true },
    },

    // ============================================================
    // ===== 校园地图 =====
    // ============================================================
    {
      path: '/locations',
      name: 'locations',
      component: () => import('@/views/location/LocationListView.vue'),
      meta: { title: '校园地图', requiresAuth: false },
    },
    {
      path: '/location/:id',
      name: 'location-detail',
      component: () => import('@/views/location/LocationDetailView.vue'),
      meta: { title: '地点详情', requiresAuth: false },
    },
    {
      path: '/route-planner',
      name: 'route-planner',
      component: () => import('@/views/location/RoutePlannerView.vue'),
      meta: { title: '路径规划', requiresAuth: false },
    },

    // ============================================================
    // ===== AI智慧助手 =====
    // ============================================================
    {
      path: '/ai-chat',
      name: 'ai-chat',
      component: () => import('@/views/ai/AIChatView.vue'),
      meta: { title: 'AI智慧助手', requiresAuth: true },
    },

    // ============================================================
    // ===== 首页（重定向到新闻列表） =====
    // ============================================================
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { title: '首页', requiresAuth: true },
    },

    // ============================================================
    // ===== 404 兜底 =====
    // ============================================================
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '404', requiresAuth: false },
    },
  ],
})

// ============================================================
// ===== 路由守卫：未登录跳转登录页 =====
// ============================================================
router.beforeEach((to, from, next) => {
  // ⚠️ 测试阶段：直接放行所有路由（联调时恢复下面的守卫）
  // const userStore = useUserStore()
  // if (to.meta.requiresAuth && !userStore.isLoggedIn) {
  //   next({ name: 'login', query: { redirect: to.fullPath } })
  // } else {
  //   next()
  // }
  next()
})

// ===== 标题更新 =====
router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - 智慧校园门户` : '智慧校园门户'
})

export default router