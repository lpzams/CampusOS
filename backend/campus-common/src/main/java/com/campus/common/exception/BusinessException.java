package com.campus.common.exception;

import com.campus.common.api.ResultCode;

/**
 * 业务异常。
 *
 * <p>凡是"可预期的、要告诉用户具体原因"的错误，都抛这个异常，
 * 由 api 层的全局异常处理器统一捕获并转成 {@link com.campus.common.api.Result}。
 *
 * <p>用法举例：
 * <pre>
 *   if (news == null) {
 *       throw new BusinessException(ResultCode.NOT_FOUND, "新闻不存在");
 *   }
 * </pre>
 */
public class BusinessException extends RuntimeException {

    /** 错误码，前端可据此做分支处理 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
