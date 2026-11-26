package com.zorcee.smartdocserver.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
* Sa-Token权限拦截器配置（派保安巡逻）*/
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //注册Sa-Token拦截器，把@SaCheckLogin这套注解鉴权功能彻底激活
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
