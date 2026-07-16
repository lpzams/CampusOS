package com.campus.api.common;

import com.campus.common.api.Result;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 —— DDD 洋葱架构·api 层。
 *
 * <p>【为什么要有它】让所有 Controller 都不用写 try-catch。异常抛到这里，
 * 统一转成前端认识的 {@link Result} 结构，前端只需判断 code 即可。
 *
 * <p>【三类异常分工】
 * <ul>
 *   <li>{@link BusinessException}：业务可预期错误（如"新闻不存在"），原样把 code/msg 返回。</li>
 *   <li>参数校验异常：把第一条校验失败信息提取出来返回。</li>
 *   <li>其它未捕获异常：记日志 + 返回统一的"系统内部错误"，不泄露堆栈给前端。</li>
 * </ul>
 *
 * <p>【新增功能时】通常不用改这里。有新的通用异常类型时才在此追加 @ExceptionHandler。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：把业务定义的 code 和 message 原样返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @RequestBody 上的 @Valid 校验失败（如 CreateNewsCommand 的 @NotBlank） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(ResultCode.VALIDATE_FAILED.getCode(), msg);
    }

    /** 表单/query 参数校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(ResultCode.VALIDATE_FAILED.getCode(), msg);
    }

    /** 兜底：所有未预期的异常。记完整堆栈，但只给前端一句笼统提示。 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpected(Exception e) {
        log.error("系统未捕获异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
