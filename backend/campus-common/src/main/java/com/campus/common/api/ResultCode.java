package com.campus.common.api;

/**
 * 统一返回码。
 *
 * <p>约定：
 * <ul>
 *   <li>0        成功</li>
 *   <li>1xxx     通用/客户端错误</li>
 *   <li>2xxx     认证与权限</li>
 *   <li>业务错误码由各功能自行在此追加，注释标明所属模块。</li>
 * </ul>
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(0, "操作成功"),

    /** 通用失败 */
    FAILED(1000, "操作失败"),
    /** 参数校验失败 */
    VALIDATE_FAILED(1001, "参数校验失败"),
    /** 资源不存在 */
    NOT_FOUND(1002, "资源不存在"),
    /** 系统内部错误 */
    INTERNAL_ERROR(1003, "系统内部错误"),

    /** 未登录或 token 失效 */
    UNAUTHORIZED(2001, "暂未登录或登录已过期"),
    /** 无权限访问 */
    FORBIDDEN(2002, "没有相关操作权限");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
