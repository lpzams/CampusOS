import { del, get, post, put, upload } from '@/utils/request'

// 1. 认证
export const login = (data) => post('/auth/login', data)
export const smsLogin = (data) => post('/auth/login/sms', data)
export const sendSms = (data) => post('/auth/sms/send', data)
export const wechatLogin = (data) => post('/auth/login/wechat', data)
export const register = (data) => post('/auth/register', data)
export const forgotPassword = (data) => post('/auth/forgot-password', data)
export const logout = () => post('/auth/logout')
export const refreshToken = () => post('/auth/refresh')

// 2. 个人信息
export const getProfile = () => get('/user/profile')
export const updateProfile = (data) => put('/user/profile', data)
export const uploadAvatar = (path) => upload('/user/avatar', path)
export const getStudentProfile = () => get('/user/profile/student')
export const getTeacherProfile = () => get('/user/profile/teacher')
export const verifyIdentity = (data) => post('/user/verify', data)
export const changePassword = (data) => put('/user/password', data)

// 3-4. 新闻与公告
export const favoriteNews = (id) => post(`/news/favorite/${id}`)
export const unfavoriteNews = (id) => del(`/news/favorite/${id}`)
export const getFavoriteNews = (data) => get('/news/favorites', data)
export const publishNews = (data) => post('/admin/news', data)
export const getNotices = (data) => get('/notice/list', data)
export const getNoticeDetail = (id) => get(`/notice/detail/${id}`)
export const readNotice = (id) => post(`/notice/read/${id}`)
export const getUnreadNoticeCount = () => get('/notice/unread/count')

// 5-7. 教务
export const getSchedule = (data) => get('/course/schedule', data)
export const getTodayCourses = () => get('/course/today')
export const getFreeClassrooms = (data) => get('/course/free-classrooms', data)
export const getTeacherCourses = (id) => get(`/course/teacher/${id}`)
export const getScores = (data) => get('/score/list', data)
export const getGpa = () => get('/score/gpa')
export const getScoreStatistics = () => get('/score/statistics')
export const getExams = () => get('/exam/list')
export const getExamCalendar = (month) => get('/exam/calendar', { month })

// 8-10. 校园生活
export const getPendingPayments = () => get('/payment/pending')
export const createPaymentOrder = (data) => post('/payment/order', data)
export const getPaymentRecords = (data) => get('/payment/records', data)
export const rechargeElectricity = (data) => post('/payment/electricity', data)
export const getCardInfo = () => get('/card/info')
export const getCardTransactions = (data) => get('/card/transactions', data)
export const reportCardLoss = () => post('/card/loss')
export const cancelCardLoss = () => post('/card/unloss')
export const rechargeCard = (data) => post('/card/recharge', data)
export const getDormitoryInfo = () => get('/dormitory/info')
export const getDormitoryNotices = () => get('/dormitory/notice')
export const getDormitoryUtility = () => get('/dormitory/utility')

// 11. 报修
export const submitRepair = (data) => post('/repair/submit', data)
export const getRepairs = (data) => get('/repair/list', data)
export const getRepairDetail = (id) => get(`/repair/detail/${id}`)
export const evaluateRepair = (data) => post('/repair/evaluate', data)

// 12. 二手交易
export const getProducts = (data) => get('/product/list', data)
export const getProductDetail = (id) => get(`/product/detail/${id}`)
export const publishProduct = (data) => post('/product', data)
export const favoriteProduct = (id) => post(`/product/favorite/${id}`)
export const createProductOrder = (data) => post('/product/order', data)

// 13. 校园活动
export const getActivities = (data) => get('/activity/list', data)
export const getActivityDetail = (id) => get(`/activity/detail/${id}`)
export const registerActivity = (data) => post('/activity/register', data)
export const cancelActivity = (id) => del(`/activity/register/${id}`)
export const checkinActivity = (data) => post('/activity/checkin', data)
export const getMyActivities = () => get('/activity/my')

// 14. 校园地图
export const getLocations = (data) => get('/location/list', data)
export const getLocationDetail = (id) => get(`/location/detail/${id}`)
export const searchLocations = (keyword) => get('/location/search', { keyword })
export const getRoute = (data) => post('/location/route', data)

// 15. AI 助手
export const chat = (data) => post('/ai/chat', data)
export const getHotQuestions = () => get('/ai/hot-questions')
export const getProcess = (data) => post('/ai/process', data)
export const getRecommendations = (type) => get('/ai/recommend', { type })
