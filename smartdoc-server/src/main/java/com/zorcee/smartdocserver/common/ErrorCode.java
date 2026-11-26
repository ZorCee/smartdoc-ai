package com.zorcee.smartdocserver.common;

import lombok.Getter;

/**
 * 企业级统一错误码枚举（对标 ruoyi-vue-pro 规范）
 */
@Getter
public enum ErrorCode {

    // ===== 通用客户端 / 权限错误 (4xx) =====
    BAD_REQUEST(400, "请求参数不正确"),
    UNAUTHORIZED(401, "未登录或登录状态已过期"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "请求的资源不存在"),

    // ===== 业务模块错误 (10000+) =====
    USER_EXISTS(10001, "用户名已被注册"),
    USER_NOT_FOUND_OR_PWD_ERR(10002, "账号或密码错误"),
    KB_NOT_FOUND(20001, "知识库不存在"),
    KB_ACCESS_DENIED(20002, "无权访问或修改该知识库"),
    DOC_PARSE_ERROR(30001, "文档解析失败，请检查文件格式"),
    AI_SERVICE_ERROR(40001, "大模型服务调用异常，请稍后重试"),

    // ===== 系统级未知错误 (500) =====
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后再试");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}