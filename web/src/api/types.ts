/**
 * 与后端约定的「通用返回结构」类型定义。
 *
 * 一一对应 backend/campus-common 里的三个类：
 *   Result.java / PageResult.java / PageQuery.java
 *
 * 【新增功能时】不用改这里；你功能自己的 DTO 类型写在 api/你的功能.ts 里。
 */

/** 统一返回结构（对应 Result.java）。code=200 成功，其余失败，msg 为提示语。 */
export interface Result<T = unknown> {
  code: number
  msg: string
  data: T
}

/** 统一分页结构（对应 PageResult.java）。所有分页接口都长这样。 */
export interface PageResult<T> {
  /** 当前页码，从 1 开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
  /** 当前页数据 */
  list: T[]
}

/** 分页查询参数基类（对应 PageQuery.java）。各功能的查询参数 extends 它。 */
export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录响应数据（对应后端 /auth/login 的 data 字段） */
export interface LoginResponse {
  token: string
  userId: number
  username: string
  realName: string
  userType: 1 | 2 | 3   // 1-学生 2-教师 3-管理员
  avatar?: string
  expiresIn: number     // token 有效期（毫秒）
}

/** 短信验证码登录请求参数 */
export interface SmsLoginParams {
  phone: string
  code: string
}

export type SmsType = 'LOGIN' | 'REGISTER' | 'BIND' | 'FORGOT'

/** 发送短信验证码请求参数（1.3 新增 type 字段） */
export interface SendSmsParams {
  phone: string
  type: SmsType   // LOGIN-登录 REGISTER-注册 BIND-绑定
}

// ========== 1.4 微信授权登录（小程序） ==========

/** 微信用户信息（小程序 wx.getUserInfo 返回的格式） */
export interface WechatUserInfo {
  nickName: string
  avatarUrl: string
  // 以下字段可选，按需扩展
  gender?: number
  city?: string
  province?: string
  country?: string
}

/** 微信登录请求参数 */
export interface WechatLoginParams {
  code: string                 // 微信登录 code（wx.login 获取）
  userInfo?: WechatUserInfo    // 用户信息（可选，按需传）
}

/** 微信登录响应（与 LoginResponse 结构一致，但可能多字段） */
export interface WechatLoginResponse {
  token: string
  userId: number
  username: string
  realName: string
  userType: 1 | 2 | 3
  avatar?: string
  expiresIn: number
  /** 是否首次登录（可选字段，便于前端做引导） */
  isFirstLogin?: boolean
}

/** 用户类型（注册时可选） */
export type RegisterUserType = 1 | 2 // 1-学生 2-教师

/** 用户注册请求参数 */
export interface RegisterParams {
  username: string   // 学号/工号
  password: string
  realName: string
  phone: string
  email: string
  userType: RegisterUserType
  department: string
  studentId: string  // 学生：学号 教师：工号
}

/** 用户注册响应（通常只返回注册成功信息，不返回完整用户信息） */
export interface RegisterResponse {
  userId: number
  username: string
  realName: string
  message?: string   // 可选：后端返回的提示信息
}

/** 忘记密码请求参数 */
export interface ForgotPasswordParams {
  username: string
  phone: string
  code: string
  newPassword: string
}

/** 刷新Token响应 */
export interface RefreshTokenResponse {
  token: string
}

/** 用户性别 */
export type Gender = '男' | '女' | '保密'

/** 用户状态：0-正常 1-冻结 */
export type UserStatus = 0 | 1

/** 用户完整个人信息（对应后端 UserProfileVO） */
export interface UserProfile {
  id: number
  username: string
  realName: string
  gender: Gender
  phone: string
  email: string
  avatar: string
  userType: 1 | 2 | 3  // 1-学生 2-教师 3-管理员
  department: string
  major: string         // 专业（学生）/ 研究方向（教师）
  className: string     // 班级（仅学生）
  studentId: string     // 学号（学生）/ 工号（教师）
  enrollmentYear: string // 入学年份（学生）/ 入职年份（教师）
  status: UserStatus
  createdTime: string   // ISO 时间字符串
}

/** 修改个人信息请求参数（只允许修改这四个字段） */
export interface UpdateProfileParams {
  realName: string
  phone: string
  email: string
  gender: '男' | '女' | '保密'
}

/** 修改个人信息响应（返回更新后的完整用户信息） */
export type UpdateProfileResponse = UserProfile

/** 头像上传响应 */
export interface AvatarUploadResponse {
  avatar: string   // 新头像的 URL
}

/** 学生详细信息（学生专属拓展字段） */
export interface StudentProfile {
  userId: number
  studentId: string
  realName: string
  gender: '男' | '女' | '保密'
  department: string
  major: string
  className: string
  enrollmentYear: string
  dormitory: string      // 宿舍
  advisor: string        // 辅导员
}

// ===== 2.5 获取教师详细信息 =====

export interface TeacherProfile {
  userId: number
  teacherId: string
  realName: string
  gender: '男' | '女' | '保密'
  department: string
  title: string
  office: string
  phone: string
  email: string
  researchArea: string
  introduction: string
}

// ===== 2.6 实名认证 =====

export type VerifyStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface VerifyParams {
  realName: string
  idCard: string
  idCardFront: string
  idCardBack: string
  studentId: string
}

export interface VerifyResponse {
  verifyId: number
  status: VerifyStatus
  statusDesc: string
  submitTime: string
  expectedFinishTime: string
}

// ===== 2.7 修改密码 =====

export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
}

// ============================================================
// ===== 3.1 新闻分类 =====
// ============================================================

/** 新闻分类 */
export interface NewsCategory {
  id: number
  name: string
  description: string
  count: number
  sortOrder: number
}

// ============================================================
// ===== 3.1 新闻列表 =====
// ============================================================

/** 新闻列表项（列表页展示用） */
export interface NewsListItem {
  id: number
  title: string
  summary: string
  coverImage: string
  category: string
  categoryId: number
  viewCount: number
  isTop: boolean
  createTime: string
}

/** 新闻列表响应 */
export interface NewsListResponse {
  total: number
  page: number
  size: number
  pages: number
  list: NewsListItem[]
}

/** 新闻列表查询参数 */
export interface NewsListParams {
  categoryId?: number
  keyword?: string
  page?: number
  size?: number
}

// ============================================================
// ===== 3.2 新闻详情 =====
// ============================================================

/** 新闻详情 */
export interface NewsDetail {
  id: number
  title: string
  content: string
  coverImage: string
  category: string
  categoryId: number
  author: string
  viewCount: number
  favoriteCount: number
  isFavorite: boolean
  isTop: boolean
  createTime: string
  updateTime: string
}

// ============================================================
// ===== 3.4 ~ 3.6 收藏 =====
// ============================================================

/** 收藏新闻项 */
export interface FavoriteNewsItem {
  id: number
  title: string
  summary: string
  coverImage: string
  category: string
  categoryId: number
  viewCount: number
  favoriteTime: string
  createTime: string
}

/** 收藏列表响应 */
export interface FavoriteListResponse {
  total: number
  page: number
  size: number
  list: FavoriteNewsItem[]
}

/** 收藏响应 */
export interface FavoriteResponse {
  favoriteId: number
  newsId: number
  favoriteTime: string
}

// ============================================================
// ===== 3.7 发布新闻 =====
// ============================================================

/** 发布新闻请求参数 */
export interface PublishNewsParams {
  title: string
  content: string
  coverImage: string
  categoryId: number
  summary: string
  isTop: boolean
  isPublished: boolean
}

/** 发布新闻响应 */
export interface PublishNewsResponse {
  id: number
  title: string
  status: string
  createTime: string
}

// ============================================================
// ===== 模块4：校园公告通知 =====
// ============================================================

/** 公告类型 */
export type NoticeType = 'SCHOOL' | 'DEPT'

/** 公告类型描述映射 */
export const NoticeTypeMap: Record<NoticeType, string> = {
  SCHOOL: '学校公告',
  DEPT: '院系公告',
}

/** 公告列表项 */
export interface NoticeItem {
  id: number
  title: string
  type: NoticeType
  typeDesc: string
  department: string
  summary: string
  isTop: boolean
  isRead: boolean
  createTime: string
  deadline: string
}

/** 公告列表响应 */
export interface NoticeListResponse {
  total: number
  page: number
  size: number
  list: NoticeItem[]
}

/** 公告列表查询参数 */
export interface NoticeListParams {
  type?: NoticeType | ''
  department?: string
  page?: number
  size?: number
}

/** 公告附件 */
export interface NoticeAttachment {
  id: number
  name: string
  url: string
  size: number
}

/** 公告详情 */
export interface NoticeDetail {
  id: number
  title: string
  content: string
  type: NoticeType
  typeDesc: string
  department: string
  isTop: boolean
  isRead: boolean
  readCount: number
  createTime: string
  updateTime: string
  deadline: string
  attachments: NoticeAttachment[]
}

/** 标记已读响应 */
export interface NoticeReadResponse {
  noticeId: number
  readTime: string
}

/** 未读公告数量 */
export interface UnreadCountResponse {
  total: number
  school: number
  department: number
}

// ============================================================
// ===== 模块5：课程管理 =====
// ============================================================

/** 课程信息 */
export interface Course {
  id: number
  name: string
  teacher: string
  classroom: string
  building: string
  weeks: string
  color: string
  credit: number
}

/** 课表时段课程（一个时段可能有多个课程，或为空） */
export interface ScheduleSlot {
  dayOfWeek: number
  dayName: string
  timeSlot: string
  courses: Course[]
}

/** 课表响应 */
export interface ScheduleResponse {
  semester: string
  week: number
  studentId: string
  schedule: ScheduleSlot[]
}

/** 今日课程参数 */
export interface TodayCoursesParams {
  semester?: string
  week?: number
}

/** 今日课程项（比 Course 多 status 字段） */
export interface TodayCourse extends Course {
  status: '未开始' | '进行中' | '已结束'
  timeSlot: string
}

/** 今日课程响应 */
export interface TodayCoursesResponse {
  date: string
  dayOfWeek: number
  week: number
  semester: string
  courses: TodayCourse[]
}

/** 空闲教室查询参数 */
export interface FreeClassroomsParams {
  building?: string
  date?: string      // YYYY-MM-DD
  timeSlot?: string  // 如：1-2节
}

/** 空闲教室信息 */
export interface FreeClassroom {
  classroom: string
  building: string
  floor: number
  capacity: number
  type: string
  status: '空闲' | '占用'
}

/** 空闲教室响应 */
export interface FreeClassroomsResponse {
  date: string
  timeSlot: string
  building: string
  freeClassrooms: FreeClassroom[]
}

/** 教师授课课程项 */
export interface TeacherCourseItem {
  id: number
  name: string
  courseCode: string
  credit: number
  classroom: string
  timeSlot: string
  students: number
  schedule: string
}

/** 教师授课列表响应 */
export interface TeacherCoursesResponse {
  teacherId: number
  teacherName: string
  department: string
  semester: string
  courses: TeacherCourseItem[]
}

// ============================================================
// ===== 模块6：成绩查询 =====
// ============================================================

/** 成绩项 */
export interface ScoreItem {
  courseName: string
  courseCode: string
  credit: number
  score: number
  grade: string
  type: string        // 考试 / 平时
  examTime: string    // YYYY-MM-DD
  isPassed: boolean
}

/** 成绩列表响应 */
export interface ScoreListResponse {
  semester: string
  gpa: number
  totalCredits: number
  passedCredits: number
  scores: ScoreItem[]
}

/** 成绩列表查询参数 */
export interface ScoreListParams {
  semester?: string
  type?: 'EXAM' | 'USUAL'
}

/** GPA 历史记录项 */
export interface GpaHistoryItem {
  semester: string
  gpa: number
  credits: number
}

/** GPA 响应 */
export interface GpaResponse {
  overallGpa: number
  currentSemesterGpa: number
  semester: string
  totalCredits: number
  passedCredits: number
  failedCredits: number
  rank: number
  totalStudents: number
  history: GpaHistoryItem[]
}

/** 成绩统计分析 - 学期统计项 */
export interface ScoreBySemesterItem {
  semester: string
  avgScore: number
  credit: number
}

/** 成绩统计分析 - 类别统计 */
export interface ScoreByCategory {
  [key: string]: number  // 专业课: 85.2, 公共课: 78.5
}

/** 成绩统计分析响应 */
export interface ScoreStatisticsResponse {
  totalCourses: number
  avgScore: number
  maxScore: number
  minScore: number
  distribution: {
    [grade: string]: number  // 优秀: 4, 良好: 3
  }
  bySemester: ScoreBySemesterItem[]
  byCategory: ScoreByCategory
}

// ============================================================
// ===== 模块7：考试安排 =====
// ============================================================

/** 考试状态码 */
export type ExamStatusCode = 'PENDING' | 'COMPLETED'

/** 考试状态描述映射 */
export const ExamStatusMap: Record<ExamStatusCode, string> = {
  PENDING: '待考试',
  COMPLETED: '已考完',
}

/** 考试项 */
export interface ExamItem {
  id: number
  courseName: string
  courseCode: string
  examDate: string      // YYYY-MM-DD
  examTime: string      // HH:MM-HH:MM
  building: string
  classroom: string
  seatNumber: string
  status: string        // 待考试 / 已考完
  statusCode: ExamStatusCode
}

/** 考试日历 - 某天的考试事件 */
export interface ExamDayEvent {
  date: string
  dayOfWeek: number
  exams: {
    id: number
    courseName: string
    time: string
    classroom: string
    status: ExamStatusCode
  }[]
}

/** 考试日历响应 */
export interface ExamCalendarResponse {
  year: number
  month: number
  events: ExamDayEvent[]
}

/** 考试日历查询参数 */
export interface ExamCalendarParams {
  month: string         // YYYY-MM
}

// ============================================================
// ===== 模块8：校园缴费 =====
// ============================================================

/** 缴费状态码 */
export type PaymentStatusCode = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED'

/** 缴费状态描述映射 */
export const PaymentStatusMap: Record<PaymentStatusCode, string> = {
  PENDING: '待缴费',
  PAID: '已缴费',
  OVERDUE: '已逾期',
  CANCELLED: '已取消',
}

/** 缴费类型码 */
export type PaymentTypeCode = 'TUITION' | 'DORMITORY' | 'BOOK' | 'OTHER'

/** 支付方式 */
export type PayMethod = 'WECHAT' | 'ALIPAY' | 'BANK'

/** 支付方式描述映射 */
export const PayMethodMap: Record<PayMethod, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  BANK: '银行卡',
}

/** 待缴费项 */
export interface PendingPayment {
  id: number
  type: string
  typeCode: PaymentTypeCode
  amount: number
  deadline: string
  status: string
  statusCode: PaymentStatusCode
  description: string
  lateFee: number
}

/** 创建订单请求参数 */
export interface CreateOrderParams {
  paymentId: number
  payMethod: PayMethod
}

/** 创建订单响应 */
export interface CreateOrderResponse {
  orderId: string
  amount: number
  payMethod: string
  status: string
  payUrl: string
  qrCode: string
  expireTime: string
}

/** 缴费记录项 */
export interface PaymentRecord {
  id: number
  type: string
  amount: number
  status: string
  payMethod: string
  payTime: string
  orderId: string
}

/** 缴费记录列表响应 */
export interface PaymentRecordsResponse {
  total: number
  page: number
  size: number
  list: PaymentRecord[]
}

/** 电费充值请求参数 */
export interface ElectricityRechargeParams {
  dormitoryId: string
  amount: number
  payMethod: PayMethod
}

/** 电费充值响应 */
export interface ElectricityRechargeResponse {
  orderId: string
  dormitoryId: string
  amount: number
  oldBalance: number
  newBalance: number
  payTime: string
}

// ============================================================
// ===== 模块9：校园卡服务 =====
// ============================================================

/** 校园卡状态码 */
export type CardStatusCode = 'NORMAL' | 'LOST' | 'FROZEN' | 'EXPIRED'

/** 校园卡状态描述映射 */
export const CardStatusMap: Record<CardStatusCode, string> = {
  NORMAL: '正常',
  LOST: '挂失',
  FROZEN: '冻结',
  EXPIRED: '过期',
}

/** 交易类型码 */
export type TransactionTypeCode = 'CONSUME' | 'RECHARGE' | 'REFUND'

/** 交易类型描述映射 */
export const TransactionTypeMap: Record<TransactionTypeCode, string> = {
  CONSUME: '消费',
  RECHARGE: '充值',
  REFUND: '退款',
}

/** 校园卡信息 */
export interface CardInfo {
  cardId: string
  userId: number
  realName: string
  balance: number
  status: string
  statusCode: CardStatusCode
  expireTime: string
  lastRechargeTime: string
  lastConsumeTime: string
}

/** 交易记录项 */
export interface TransactionItem {
  id: number
  type: string
  typeCode: TransactionTypeCode
  amount: number        // 正数=充值/退款，负数=消费
  balance: number
  merchant: string
  description: string
  createTime: string
}

/** 交易记录列表响应 */
export interface TransactionListResponse {
  total: number
  page: number
  size: number
  list: TransactionItem[]
}

/** 交易记录查询参数 */
export interface TransactionListParams {
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

/** 挂失/解挂响应 */
export interface CardLossResponse {
  cardId: string
  status: string
  statusCode: CardStatusCode
  lossTime?: string
  unlossTime?: string
}

/** 校园卡充值请求参数 */
export interface CardRechargeParams {
  amount: number
  payMethod: PayMethod
}

/** 校园卡充值响应 */
export interface CardRechargeResponse {
  cardId: string
  amount: number
  oldBalance: number
  newBalance: number
  orderId: string
  payTime: string
}

// ============================================================
// ===== 模块10：宿舍管理 =====
// ============================================================

/** 宿舍类型码 */
export type DormitoryTypeCode = 'FOUR' | 'SIX' | 'EIGHT'

/** 宿舍类型描述映射 */
export const DormitoryTypeMap: Record<DormitoryTypeCode, string> = {
  FOUR: '四人间',
  SIX: '六人间',
  EIGHT: '八人间',
}

/** 宿舍成员 */
export interface DormitoryMember {
  userId: number
  realName: string
  phone: string
  isRoomLeader: boolean
  avatar: string
}

/** 宿舍信息 */
export interface DormitoryInfo {
  id: number
  building: string
  room: string
  type: string
  typeCode: DormitoryTypeCode
  members: DormitoryMember[]
  facilities: string[]
  electricityBalance: number
  waterBalance: number
}

/** 宿舍公告 */
export interface DormitoryNotice {
  id: number
  title: string
  content: string
  type: string
  createTime: string
}

/** 水电消耗历史记录 */
export interface UtilityHistory {
  month: string
  consumption: number
  cost: number
}

/** 水电余额响应 */
export interface UtilityResponse {
  dormitoryId: string
  electricityBalance: number
  waterBalance: number
  lastUpdateTime: string
  electricityHistory: UtilityHistory[]
}

// ============================================================
// ===== 模块11：校园报修 =====
// ============================================================

/** 报修状态码 */
export type RepairStatusCode =
  | 'PENDING'      // 待接单
  | 'PROCESSING'   // 处理中
  | 'COMPLETED'    // 已完成
  | 'CANCELLED'    // 已取消

/** 报修状态描述映射 */
export const RepairStatusMap: Record<RepairStatusCode, string> = {
  PENDING: '待接单',
  PROCESSING: '处理中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

/** 报修类型码 */
export type RepairTypeCode =
  | 'WATER_ELECTRIC'  // 水电
  | 'EQUIPMENT'       // 设备
  | 'FURNITURE'       // 家具
  | 'NETWORK'         // 网络
  | 'OTHER'           // 其他

/** 报修类型描述映射 */
export const RepairTypeMap: Record<RepairTypeCode, string> = {
  WATER_ELECTRIC: '水电',
  EQUIPMENT: '设备',
  FURNITURE: '家具',
  NETWORK: '网络',
  OTHER: '其他',
}

/** 报修提交请求参数 */
export interface SubmitRepairParams {
  type: string
  title: string
  description: string
  images: string[]
  building: string
  room: string
  contactPhone: string
}

/** 报修提交响应 */
export interface SubmitRepairResponse {
  repairId: number
  title: string
  status: string
  statusCode: RepairStatusCode
  createTime: string
  expectedTime: string
}

/** 报修列表项 */
export interface RepairListItem {
  id: number
  title: string
  type: string
  typeCode: RepairTypeCode
  status: string
  statusCode: RepairStatusCode
  statusDesc: string
  createTime: string
  expectedTime: string
  handler: string | null
  handlerPhone: string | null
  completeTime?: string
  isEvaluated?: boolean
}

/** 报修进度项 */
export interface RepairProgressItem {
  time: string
  status: string
  content: string
}

/** 报修详情 */
export interface RepairDetail {
  id: number
  title: string
  type: string
  typeCode: RepairTypeCode
  description: string
  images: string[]
  building: string
  room: string
  contactPhone: string
  status: string
  statusCode: RepairStatusCode
  statusDesc: string
  createTime: string
  updateTime: string
  expectedTime: string
  handler: string | null
  handlerPhone: string | null
  progress: RepairProgressItem[]
  isEvaluated?: boolean
}

/** 评价请求参数 */
export interface EvaluateParams {
  repairId: number
  score: number      // 1-5
  content: string
}

/** 评价响应 */
export interface EvaluateResponse {
  repairId: number
  score: number
  content: string
  evaluateTime: string
}

// ============================================================
// ===== 模块12：二手交易 =====
// ============================================================

/** 商品状态码（0-在售 1-已售） */
export type ProductStatusCode = 0 | 1

/** 商品状态描述映射 */
export const ProductStatusMap: Record<ProductStatusCode, string> = {
  0: '在售',
  1: '已售',
}

/** 订单状态码 */
export type OrderStatusCode =
  | 'PENDING'      // 待确认
  | 'CONFIRMED'    // 已确认
  | 'COMPLETED'    // 已完成
  | 'CANCELLED'    // 已取消

/** 订单状态描述映射 */
export const OrderStatusMap: Record<OrderStatusCode, string> = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

/** 商品列表项 */
export interface ProductItem {
  id: number
  title: string
  price: number
  originalPrice: number
  description: string
  coverImage: string
  category: string
  categoryId: number
  status: string
  statusCode: ProductStatusCode
  viewCount: number
  createTime: string
  sellerId: number
  sellerName: string
  sellerAvatar: string
}

/** 商品列表响应 */
export interface ProductListResponse {
  total: number
  page: number
  size: number
  list: ProductItem[]
}

/** 商品列表查询参数 */
export interface ProductListParams {
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
  status?: ProductStatusCode
  page?: number
  size?: number
}

/** 卖家信息 */
export interface ProductSeller {
  userId: number
  realName: string
  avatar: string
  phone: string
  wechat: string
  studentId: string
}

/** 商品详情 */
export interface ProductDetail {
  id: number
  title: string
  price: number
  originalPrice: number
  description: string
  images: string[]
  category: string
  categoryId: number
  status: string
  statusCode: ProductStatusCode
  viewCount: number
  favoriteCount: number
  isFavorite: boolean
  createTime: string
  updateTime: string
  seller: ProductSeller
}

/** 发布商品请求参数 */
export interface PublishProductParams {
  title: string
  price: number
  description: string
  categoryId: number
  images: string[]
  contactPhone: string
  wechat: string
}

/** 发布商品响应 */
export interface PublishProductResponse {
  id: number
  title: string
  price: number
  status: string
  createTime: string
  auditStatus: string
  expectedAuditTime: string
}

/** 收藏响应 */
export interface ProductFavoriteResponse {
  favoriteId: number
  productId: number
  favoriteTime: string
}

/** 创建订单请求参数 */
export interface CreateProductOrderParams {
  productId: number
  message: string
}

/** 创建订单响应 */
export interface CreateProductOrderResponse {
  orderId: string
  productId: number
  productTitle: string
  price: number
  buyerId: number
  sellerId: number
  sellerPhone: string
  status: string
  statusCode: OrderStatusCode
  createTime: string
  message: string
}

// ============================================================
// ===== 模块13：校园活动 =====
// ============================================================

/** 活动分类码 */
export type ActivityCategoryCode = 'SPORTS' | 'CULTURE' | 'ACADEMIC' | 'VOLUNTEER'

/** 活动分类描述映射 */
export const ActivityCategoryMap: Record<ActivityCategoryCode, string> = {
  SPORTS: '体育',
  CULTURE: '文艺',
  ACADEMIC: '学术',
  VOLUNTEER: '志愿',
}

/** 活动状态码 */
export type ActivityStatusCode = 'UPCOMING' | 'ONGOING' | 'ENDED'

/** 活动状态描述映射 */
export const ActivityStatusMap: Record<ActivityStatusCode, string> = {
  UPCOMING: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束',
}

/** 活动报名状态码 */
export type ActivityRegisterStatusCode = 'REGISTERED' | 'CANCELLED'

/** 活动列表项 */
export interface ActivityItem {
  id: number
  title: string
  category: string
  categoryCode: ActivityCategoryCode
  coverImage: string
  startTime: string
  endTime: string
  location: string
  maxParticipants: number
  currentParticipants: number
  status: string
  statusCode: ActivityStatusCode
  isRegistered: boolean
  createTime: string
}

/** 活动列表响应 */
export interface ActivityListResponse {
  total: number
  page: number
  size: number
  list: ActivityItem[]
}

/** 活动列表查询参数 */
export interface ActivityListParams {
  category?: ActivityCategoryCode | ''
  status?: ActivityStatusCode | ''
  page?: number
  size?: number
}

/** 活动详情 */
export interface ActivityDetail {
  id: number
  title: string
  category: string
  categoryCode: ActivityCategoryCode
  coverImage: string
  content: string
  startTime: string
  endTime: string
  location: string
  maxParticipants: number
  currentParticipants: number
  status: string
  statusCode: ActivityStatusCode
  isRegistered: boolean
  organizer: string
  contactPhone: string
  createTime: string
  updateTime: string
}

/** 活动报名请求参数 */
export interface ActivityRegisterParams {
  activityId: number
  remark?: string
}

/** 活动报名响应 */
export interface ActivityRegisterResponse {
  registerId: number
  activityId: number
  activityTitle: string
  registerTime: string
  status: string
  statusCode: ActivityRegisterStatusCode
  checkinCode: string
}

/** 活动签到请求参数 */
export interface ActivityCheckinParams {
  activityId: number
  code: string
}

/** 活动签到响应 */
export interface ActivityCheckinResponse {
  activityId: number
  activityTitle: string
  checkinTime: string
  status: string
}

/** 我的活动项 */
export interface MyActivityItem {
  id: number
  title: string
  category: string
  categoryCode: ActivityCategoryCode
  coverImage: string
  startTime: string
  endTime: string
  location: string
  status: string
  statusCode: ActivityRegisterStatusCode
  registerTime: string
  isCheckin: boolean
}

// ============================================================
// ===== 模块14：校园地图导航 =====
// ============================================================

/** 地点分类码 */
export type LocationCategoryCode =
  | 'BUILDING'   // 教学楼
  | 'LIBRARY'    // 图书馆
  | 'CANTEEN'    // 食堂
  | 'DORMITORY'  // 宿舍
  | 'GYM'        // 体育馆
  | 'OFFICE'     // 行政楼
  | 'OTHER'      // 其他

/** 地点分类描述映射 */
export const LocationCategoryMap: Record<LocationCategoryCode, string> = {
  BUILDING: '教学楼',
  LIBRARY: '图书馆',
  CANTEEN: '食堂',
  DORMITORY: '宿舍',
  GYM: '体育馆',
  OFFICE: '行政楼',
  OTHER: '其他',
}

/** 地点列表项 */
export interface LocationItem {
  id: number
  name: string
  category: string
  categoryCode: LocationCategoryCode
  longitude: number
  latitude: number
  address: string
  building: string
  image: string
  description: string
}

/** 地点列表响应（直接返回数组） */
export type LocationListResponse = LocationItem[]

/** 地点列表查询参数 */
export interface LocationListParams {
  category?: LocationCategoryCode | ''
}

/** 楼层信息 */
export interface LocationFloor {
  floor: number
  rooms: string[]
}

/** 地点详情 */
export interface LocationDetail {
  id: number
  name: string
  category: string
  categoryCode: LocationCategoryCode
  longitude: number
  latitude: number
  address: string
  building: string
  image: string
  description: string
  facilities: string[]
  floors: LocationFloor[]
  openTime: string
}

/** 地点搜索结果项 */
export interface LocationSearchResult {
  id: number
  name: string
  category: string
  categoryCode: LocationCategoryCode
  longitude: number
  latitude: number
  address: string
  distance: number   // 距离当前位置（米）
}

/** 地点搜索响应（直接返回数组） */
export type LocationSearchResponse = LocationSearchResult[]

/** 路径规划请求参数 */
export interface RoutePlanParams {
  fromLng: number
  fromLat: number
  toLng: number
  toLat: number
  mode: 'walk' | 'drive'
}

/** 路径规划 - 步骤 */
export interface RouteStep {
  instruction: string
  distance: number
  direction: string
}

/** 路径规划响应 */
export interface RoutePlanResponse {
  distance: number
  duration: number
  mode: string
  start: {
    name: string
    longitude: number
    latitude: number
  }
  end: {
    name: string
    longitude: number
    latitude: number
  }
  steps: RouteStep[]
}

// ============================================================
// ===== 模块15：AI智慧助手 =====
// ============================================================

/** AI 对话消息 */
export interface AIChatMessage {
  role: 'user' | 'assistant'
  content: string
}

/** AI 问答请求参数 */
export interface AIChatParams {
  question: string
  history?: AIChatMessage[]
}

/** AI 问答响应 */
export interface AIChatResponse {
  answer: string
  relatedQuestions: string[]
  sessionId: string
  createTime: string
}

/** 热门问题 */
export interface HotQuestion {
  id: number
  title: string
  category: string
  viewCount: number
}

/** 热门问题响应（直接返回数组） */
export type HotQuestionsResponse = HotQuestion[]