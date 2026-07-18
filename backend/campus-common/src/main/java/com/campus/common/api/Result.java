package com.campus.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结构
 * <p>
 * 所有 Controller 统一返回 Result，前端统一解析
 * <p>
 * 用法：
 * <pre>
 * // 成功返回数据
 * return Result.success(data);
 * // 成功无数据
 * return Result.success();
 * // 业务异常
 * throw new BusinessException(ResultCode.USERNAME_EXISTS);
 * // 自定义消息
 * throw new BusinessException("自定义错误消息");
 * </pre>
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    // ===== 成功 =====

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    // ===== 失败 =====

    public static <T> Result<T> fail() {
        return fail(ResultCode.FAILED);
    }

    public static <T> Result<T> fail(String msg) {
        return fail(ResultCode.FAILED.getCode(), msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }
}