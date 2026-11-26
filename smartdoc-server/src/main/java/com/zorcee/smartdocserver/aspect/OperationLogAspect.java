package com.zorcee.smartdocserver.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.zorcee.smartdocserver.annotation.OperationLog;
import com.zorcee.smartdocserver.entity.SysOperationLog;
import com.zorcee.smartdocserver.entity.User;
import com.zorcee.smartdocserver.mapper.SysOperationLogMapper;
import com.zorcee.smartdocserver.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 系统操作审计日志 AOP 切面（对标 ruoyi-vue-pro）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper sysOperationLogMapper;
    private final UserMapper userMapper;

    @Qualifier("asyncLogExecutor")
    private final Executor asyncLogExecutor;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 1. 必须在主线程中提取 ThreadLocal 上下文信息（异步线程无法获取 Request 和 Sa-Token）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        String requestUrl = request != null ? request.getRequestURI() : "";
        String requestMethod = request != null ? request.getMethod() : "";
        String ipAddress = request != null ? getClientIp(request) : "";
        Object loginIdObj = StpUtil.getLoginIdDefaultNull();

        String classMethod = joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName();
        String requestParams = buildParamsSummary(joinPoint.getArgs());

        int status = 1;
        String errorMsg = null;

        try {
            // 2. 执行目标 Controller 方法
            return joinPoint.proceed();
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            throw e; // 必须原样抛出，交给 GlobalExceptionHandler 处理
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            SysOperationLog logEntity = new SysOperationLog();
            logEntity.setModule(operationLog.module());
            logEntity.setAction(operationLog.action());
            logEntity.setMethod(classMethod);
            logEntity.setRequestUrl(requestUrl);
            logEntity.setRequestMethod(requestMethod);
            logEntity.setRequestParams(requestParams);
            logEntity.setIpAddress(ipAddress);
            logEntity.setStatus(status);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setCostTime(costTime);
            logEntity.setCreateTime(LocalDateTime.now());

            // 3. 扔进自定义线程池异步写库，绝不阻塞主业务响应
            CompletableFuture.runAsync(() -> {
                try {
                    if (loginIdObj != null) {
                        User user = userMapper.selectById(Long.parseLong(loginIdObj.toString()));
                        logEntity.setUsername(user != null ? user.getUsername() : "userId:" + loginIdObj);
                    } else {
                        logEntity.setUsername("anonymous");
                    }
                    sysOperationLogMapper.insert(logEntity);
                } catch (Exception ex) {
                    log.error("异步保存操作审计日志失败", ex);
                }
            }, asyncLogExecutor);
        }
    }

    private String buildParamsSummary(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        List<Object> safeArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }
            if (arg instanceof MultipartFile file) {
                safeArgs.add("MultipartFile(" + file.getOriginalFilename() + ")");
            } else {
                safeArgs.add(arg);
            }
        }
        String json = JSON.toJSONString(safeArgs);
        return json.length() > 1000 ? json.substring(0, 1000) : json;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}