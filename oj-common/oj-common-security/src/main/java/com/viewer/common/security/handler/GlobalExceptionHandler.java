package com.viewer.common.security.handler;

import com.viewer.common.core.domain.Result;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.QuestionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 不⽀持{}请求", requestURI, e.getMethod());
        return Result.fail(ResultCode.ERROR, "请求地址'"+ requestURI + "', 不⽀持" + e.getMethod() +"请求");
    }

    /**
     * 发生算术异常
     */
    @ExceptionHandler(ArithmeticException.class)
    public Result<?> handleArithmeticException(ArithmeticException e, HttpServletRequest request){
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 发生算术异常", requestURI);
        return Result.fail(ResultCode.ERROR, "请求地址'" + requestURI + "', 发生算术异常");
    }

    @ExceptionHandler(QuestionException.class)
    public Result<?> handleArithmeticException(QuestionException e, HttpServletRequest request){
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', {}", requestURI, e.getMsg());
        return Result.fail(e.getCode(), e.getMsg());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发⽣异常.", requestURI, e);
        return Result.fail(ResultCode.ERROR);
    }
}
