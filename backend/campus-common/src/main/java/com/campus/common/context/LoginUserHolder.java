package com.campus.common.context;

/**
 * 当前登录用户持有者（ThreadLocal）
 * <p>
 * 由认证拦截器在请求进入时设置，请求结束后自动清理。
 * 业务代码通过 {@link #get()} 获取当前用户信息。
 */
public final class LoginUserHolder {

    private LoginUserHolder() {
    }

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    public static void set(LoginUser user) {
        CONTEXT.set(user);
    }

    public static LoginUser get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }
}
