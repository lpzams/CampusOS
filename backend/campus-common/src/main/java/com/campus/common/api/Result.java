package com.campus.common.api;

import java.io.Serializable;

/**
 * 统一返回结构。所有 Controller 都返回 Result，前端只认这一种格式。
 * <p>
 * 约定：code=0 成功（见 {@link ResultCode#SUCCESS}），其余为失败；data 放业务数据；msg 给前端展示的提示。
 * 加功能时不要自己发明返回格式，一律用 Result.success(...) / Result.fail(...)。
 */
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(String msg) {
        return fail(ResultCode.FAILED.getCode(), msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }
}
