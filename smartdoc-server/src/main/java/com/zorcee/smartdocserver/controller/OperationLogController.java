package com.zorcee.smartdocserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zorcee.smartdocserver.common.Result;
import com.zorcee.smartdocserver.entity.SysOperationLog;
import com.zorcee.smartdocserver.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统操作审计日志查询接口（对标 ruoyi-vue-pro 审计日志管理）
 */
@RestController
@RequestMapping("/api/log")
@SaCheckLogin
@RequiredArgsConstructor
public class OperationLogController {

    private final SysOperationLogMapper sysOperationLogMapper;

    /**
     * 分页查询操作审计日志（支持按业务模块筛选）
     */
    @GetMapping("/page")
    public Result<Page<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module) {

        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .eq(StringUtils.hasText(module), SysOperationLog::getModule, module)
                .orderByDesc(SysOperationLog::getCreateTime);

        return Result.success(sysOperationLogMapper.selectPage(page, wrapper));
    }
}