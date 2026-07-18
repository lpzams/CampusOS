package com.campus.common.constant;

/**
 * 用户模块常量
 */
public final class UserConstants {

    private UserConstants() {
        // 私有构造器，防止实例化
    }

    // ========== 用户状态 ==========

    /** 正常 */
    public static final int STATUS_NORMAL = 0;

    /** 冻结 */
    public static final int STATUS_FROZEN = 1;

    /** 待审核 */
    public static final int STATUS_PENDING = 2;

    /** 已注销 */
    public static final int STATUS_DELETED = 3;

    // ========== 用户类型 ==========


    /** 学生 */
    public static final int TYPE_STUDENT = 1;

    /** 教师 */
    public static final int TYPE_TEACHER = 2;

    /** 管理员 */
    public static final int TYPE_ADMIN = 3;

    // ========== 性别 ==========

    /** 未知 */
    public static final int GENDER_UNKNOWN = 0;

    /** 男 */
    public static final int GENDER_MALE = 1;

    /** 女 */
    public static final int GENDER_FEMALE = 2;

    // ========== 实名认证状态 ==========

    /** 未提交 */
    public static final int VERIFY_STATUS_NONE = 0;

    /** 审核中 */
    public static final int VERIFY_STATUS_PENDING = 1;

    /** 已认证 */
    public static final int VERIFY_STATUS_PASS = 2;

    /** 已拒绝 */
    public static final int VERIFY_STATUS_REJECT = 3;

    // ========== 逻辑删除 ==========

    /** 未删除 */
    public static final int DELETED_NO = 0;

    /** 已删除 */
    public static final int DELETED_YES = 1;

    // ========== 登录失败锁定 ==========

    /** 最大登录失败次数 */
    public static final int LOGIN_FAIL_MAX_COUNT = 5;

    /** 锁定时间（分钟） */
    public static final int LOCK_MINUTES = 30;

    // ========== 角色编码 ==========

    /** 学生角色编码 */
    public static final String ROLE_STUDENT = "ROLE_STUDENT";

    /** 教师角色编码 */
    public static final String ROLE_TEACHER = "ROLE_TEACHER";

    /** 管理员角色编码 */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // ========== 默认角色ID（预置数据） ==========

    /** 学生角色ID（对应 t_role 表） */
    public static final Integer DEFAULT_ROLE_STUDENT_ID = 1;

    /** 教师角色ID（对应 t_role 表） */
    public static final Integer DEFAULT_ROLE_TEACHER_ID = 2;

    /** 管理员角色ID（对应 t_role 表） */
    public static final Integer DEFAULT_ROLE_ADMIN_ID = 3;

    // ========== 默认头像 ==========

    /** 默认头像URL */
    public static final String DEFAULT_AVATAR = "https://cdn.example.com/default-avatar.png";
}