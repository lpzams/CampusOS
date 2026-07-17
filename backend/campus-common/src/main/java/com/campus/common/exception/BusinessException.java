package com.campus.common.exception;

import com.campus.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 在业务层（Application/Domain）抛出，由 GlobalExceptionHandler 统一捕获并转换为 Result
 * <p>
 * 用法：
 * <pre>
 * // 使用预定义状态码
 * throw new BusinessException(ResultCode.USERNAME_EXISTS);
 * // 自定义消息
 * throw new BusinessException("自定义错误消息");
 * // 自定义状态码+消息
 * throw new BusinessException(1001, "自定义错误");
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.FAILED.getCode();
    }

    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
    }
}