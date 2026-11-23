package com.zhaoce50.smartdocserver.common;

import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
* 全局统一异常拦截急救中心
*/
@RestControllerAdvice
public class GlobalExceptionHandler {
    //1.拦截所有业务运行时异常(RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e){
        return Result.error(e.getMessage());
    }
    //2.专门精准拦截未登录异常（401)
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e){
        return Result.error(401,"未登录或登录状态已过期，请重新登录！");
    }
}
