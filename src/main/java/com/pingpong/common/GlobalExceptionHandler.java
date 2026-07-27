package com.pingpong.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一捕获 Controller 层抛出的各类异常，封装为标准 R<?> 响应格式返回给前端。
 * 避免异常堆栈直接暴露给前端，同时保证错误信息友好可读。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理非法参数异常（业务代码手动 throw new IllegalArgumentException）
     *
     * @param e 非法参数异常对象
     * @return 400 状态码 + 异常消息
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleIllegalArgument(IllegalArgumentException e) {
        return R.fail(400, e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 注解校验失败时触发）
     * 将所有字段的校验错误拼接成一条消息返回，格式：字段名: 错误信息; 字段名: 错误信息
     *
     * @param e 参数校验异常对象
     * @return 400 状态码 + 拼接后的校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return R.fail(400, msg);
    }

    /**
     * 处理运行时异常（业务逻辑抛出的 RuntimeException）
     * 记录错误日志并返回异常消息给前端
     *
     * @param e 运行时异常对象
     * @return 500 状态码 + 异常消息
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleRuntime(RuntimeException e) {
        log.error("运行时异常", e);
        return R.fail(500, e.getMessage());
    }

    /**
     * 兜底处理所有其他异常（系统级未知错误）
     * 记录完整错误日志，只返回通用提示，不暴露内部堆栈信息
     *
     * @param e 异常对象
     * @return 500 状态码 + 通用错误提示
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "系统内部错误");
    }
}
