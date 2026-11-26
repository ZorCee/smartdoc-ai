package com.zorcee.smartdocserver.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作审计日志注解（打在 Controller 接口上）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 所属业务模块（如：知识库管理、文档引擎） */
    String module();

    /** 操作类型（如：新增、删除、上传、问答） */
    String action();
}