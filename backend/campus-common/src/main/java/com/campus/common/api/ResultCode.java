package com.campus.common.api;

import lombok.Getter;

@Getter
public enum ResultCode {

    // ===== 通用 =====
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未授权，请重新登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    MEDIA_TYPE_UNSUPPORTED(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ===== 1. 用户认证模块 (1000-1099) =====
    USERNAME_EXISTS(1001, "用户名已存在"),
    PHONE_EXISTS(1002, "手机号已注册"),
    EMAIL_EXISTS(1003, "邮箱已被使用"),
    SMS_CODE_ERROR(1004, "验证码错误"),
    SMS_CODE_EXPIRED(1005, "验证码已过期"),
    SMS_CODE_LIMIT(1006, "验证码发送次数已达上限"),
    USER_NOT_EXIST(1007, "用户不存在"),
    USER_STATUS_FROZEN(1008, "账号已被冻结，请联系管理员"),
    USER_STATUS_DELETED(1009, "账号已注销"),
    USER_STATUS_PENDING(1010, "账号待审核，请等待管理员审核"),
    PASSWORD_ERROR(1011, "密码错误"),
    PASSWORD_WEAK(1012, "密码强度不足"),
    LOGIN_EXPIRED(1013, "登录已过期，请重新登录"),
    TOKEN_INVALID(1014, "Token无效"),
    TOKEN_EXPIRED(1015, "Token已过期"),
    ACCOUNT_LOCKED(1016, "账号已被锁定，请5分钟后重试"),
    VERIFY_CODE_ERROR(1017, "验证码错误"),
    VERIFY_CODE_EXPIRED(1018, "验证码已过期"),
    WECHAT_LOGIN_FAILED(1019, "微信登录失败"),
    PHONE_LOGIN_FAILED(1020, "手机号登录失败"),

    // ===== 2. 个人信息模块 (1100-1199) =====
    PROFILE_NOT_FOUND(1101, "个人信息不存在"),
    REAL_NAME_VERIFY_FAILED(1102, "实名认证失败"),
    ID_CARD_INVALID(1103, "身份证号格式不正确"),
    ID_CARD_ALREADY_USED(1104, "身份证号已被认证"),
    AVATAR_UPLOAD_FAILED(1105, "头像上传失败"),
    STUDENT_ID_ALREADY_USED(1106, "学号已被认证"),
    TEACHER_ID_ALREADY_USED(1107, "工号已被认证"),
    OLD_PASSWORD_ERROR(1108, "原密码错误"),
    NEW_PASSWORD_SAME(1109, "新密码不能与旧密码相同"),

    // ===== 3. 校园新闻模块 (1200-1299) =====
    NEWS_NOT_FOUND(1201, "新闻不存在"),
    NEWS_CATEGORY_NOT_FOUND(1202, "新闻分类不存在"),
    NEWS_ALREADY_PUBLISHED(1203, "新闻已发布"),
    NEWS_ALREADY_FAVORITED(1204, "已收藏该新闻"),
    NEWS_NOT_FAVORITED(1205, "未收藏该新闻"),
    NEWS_FAVORITE_FAILED(1206, "收藏失败"),
    NEWS_UNFAVORITE_FAILED(1207, "取消收藏失败"),

    // ===== 4. 公告通知模块 (1300-1399) =====
    NOTICE_NOT_FOUND(1301, "公告不存在"),
    NOTICE_ALREADY_READ(1302, "公告已读"),
    NOTICE_PUBLISH_FAILED(1303, "公告发布失败"),

    // ===== 5. 课程管理模块 (1400-1499) =====
    COURSE_NOT_FOUND(1401, "课程不存在"),
    COURSE_SCHEDULE_NOT_FOUND(1402, "课表不存在"),
    CLASSROOM_NOT_FOUND(1403, "教室不存在"),
    CLASSROOM_OCCUPIED(1404, "教室已被占用"),
    TEACHER_NOT_FOUND(1405, "教师不存在"),
    COURSE_CONFLICT(1406, "课程时间冲突"),
    SEMESTER_INVALID(1407, "学期无效"),
    WEEK_INVALID(1408, "周次无效"),

    // ===== 6. 成绩查询模块 (1500-1599) =====
    SCORE_NOT_FOUND(1501, "成绩不存在"),
    SCORE_SEMESTER_INVALID(1502, "学期无效"),
    GPA_CALCULATE_FAILED(1503, "GPA计算失败"),

    // ===== 7. 考试安排模块 (1600-1699) =====
    EXAM_NOT_FOUND(1601, "考试安排不存在"),
    EXAM_CLASH(1602, "考试时间冲突"),
    EXAM_ROOM_NOT_FOUND(1603, "考场不存在"),
    EXAM_SEAT_NOT_FOUND(1604, "座位号不存在"),

    // ===== 8. 校园缴费模块 (1700-1799) =====
    PAYMENT_NOT_FOUND(1701, "缴费单不存在"),
    PAYMENT_ALREADY_PAID(1702, "已缴费"),
    PAYMENT_OVERDUE(1703, "缴费已过期"),
    PAYMENT_AMOUNT_ERROR(1704, "缴费金额错误"),
    PAYMENT_ORDER_FAILED(1705, "创建缴费订单失败"),
    PAYMENT_CALLBACK_FAILED(1706, "支付回调处理失败"),
    ELECTRICITY_AMOUNT_ERROR(1707, "电费金额错误"),
    DORMITORY_NOT_FOUND(1708, "宿舍不存在"),

    // ===== 9. 校园卡模块 (1800-1899) =====
    CARD_NOT_FOUND(1801, "校园卡不存在"),
    CARD_ALREADY_LOST(1802, "校园卡已挂失"),
    CARD_NOT_LOST(1803, "校园卡未挂失"),
    CARD_LOSS_FAILED(1804, "挂失失败"),
    CARD_UNLOSS_FAILED(1805, "解挂失败"),
    CARD_BALANCE_INSUFFICIENT(1806, "余额不足"),
    CARD_RECHARGE_FAILED(1807, "充值失败"),
    CARD_TRANSACTION_NOT_FOUND(1808, "消费记录不存在"),
    CARD_FROZEN(1809, "校园卡已冻结"),

    // ===== 10. 宿舍管理模块 (1900-1999) =====
    DORMITORY_INFO_NOT_FOUND(1901, "宿舍信息不存在"),
    DORMITORY_MEMBER_NOT_FOUND(1902, "宿舍成员不存在"),
    DORMITORY_NOTICE_NOT_FOUND(1903, "宿舍公告不存在"),
    UTILITY_QUERY_FAILED(1904, "水电查询失败"),
    UTILITY_RECHARGE_FAILED(1905, "水电充值失败"),

    // ===== 11. 校园报修模块 (2000-2099) =====
    REPAIR_NOT_FOUND(2001, "报修单不存在"),
    REPAIR_STATUS_INVALID(2002, "报修状态无效"),
    REPAIR_IMAGE_UPLOAD_FAILED(2003, "报修图片上传失败"),
    REPAIR_EVALUATE_FAILED(2004, "评价失败"),
    REPAIR_ALREADY_EVALUATED(2005, "已评价"),
    REPAIR_CANCEL_FAILED(2006, "取消报修失败"),

    // ===== 12. 二手交易模块 (2100-2199) =====
    PRODUCT_NOT_FOUND(2101, "商品不存在"),
    PRODUCT_ALREADY_SOLD(2102, "商品已售出"),
    PRODUCT_OFFLINE(2103, "商品已下架"),
    PRODUCT_FAVORITE_FAILED(2104, "收藏失败"),
    PRODUCT_UNFAVORITE_FAILED(2105, "取消收藏失败"),
    PRODUCT_ALREADY_FAVORITED(2106, "已收藏该商品"),
    PRODUCT_NOT_FAVORITED(2107, "未收藏该商品"),
    ORDER_NOT_FOUND(2108, "订单不存在"),
    ORDER_STATUS_INVALID(2109, "订单状态无效"),
    ORDER_NOT_OWNER(2110, "无权操作此订单"),
    ORDER_ALREADY_PAID(2111, "订单已支付"),
    ORDER_PAY_FAILED(2112, "支付失败"),
    PRODUCT_IMAGE_UPLOAD_FAILED(2113, "商品图片上传失败"),

    // ===== 13. 校园活动模块 (2200-2299) =====
    ACTIVITY_NOT_FOUND(2201, "活动不存在"),
    ACTIVITY_ENDED(2202, "活动已结束"),
    ACTIVITY_NOT_STARTED(2203, "活动未开始"),
    ACTIVITY_FULL(2204, "活动报名已满"),
    ACTIVITY_ALREADY_REGISTERED(2205, "已报名该活动"),
    ACTIVITY_NOT_REGISTERED(2206, "未报名该活动"),
    ACTIVITY_REGISTER_FAILED(2207, "报名失败"),
    ACTIVITY_CANCEL_FAILED(2208, "取消报名失败"),
    ACTIVITY_CHECKIN_FAILED(2209, "签到失败"),
    ACTIVITY_ALREADY_CHECKIN(2210, "已签到"),
    ACTIVITY_EVALUATE_FAILED(2211, "评价失败"),
    ACTIVITY_ALREADY_EVALUATED(2212, "已评价"),
    ACTIVITY_CATEGORY_NOT_FOUND(2213, "活动分类不存在"),

    // ===== 14. 校园地图模块 (2300-2399) =====
    LOCATION_NOT_FOUND(2301, "地点不存在"),
    LOCATION_CATEGORY_NOT_FOUND(2302, "地点分类不存在"),
    ROUTE_PLAN_FAILED(2303, "路径规划失败"),
    NAVIGATION_FAILED(2304, "导航失败"),

    // ===== 15. AI助手模块 (2400-2499) =====
    AI_SERVICE_ERROR(2401, "AI服务异常，请稍后再试"),
    AI_QUESTION_EMPTY(2402, "问题不能为空"),
    AI_RESPONSE_TIMEOUT(2403, "AI响应超时"),
    AI_DAILY_LIMIT(2404, "今日AI问答次数已达上限"),

    // ===== 文件上传 (2500-2599) =====
    FILE_UPLOAD_FAILED(2501, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(2502, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(2503, "文件大小超出限制"),
    FILE_NOT_FOUND(2504, "文件不存在"),
    FILE_DELETE_FAILED(2505, "文件删除失败"),

    // ===== 系统权限 (2600-2699) =====
    PERMISSION_DENIED(2601, "权限不足"),
    ROLE_NOT_FOUND(2602, "角色不存在"),
    USER_ROLE_ASSIGN_FAILED(2603, "分配角色失败"),
    USER_ROLE_REMOVE_FAILED(2604, "移除角色失败"),
    OPERATION_LOG_FAILED(2605, "操作日志记录失败"),
    SYSTEM_ERROR(2700, "系统异常，请稍后再试");


    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据 code 获取枚举
     */
    public static ResultCode fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
}