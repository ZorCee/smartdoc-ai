package com.zhaoce50.smartdocserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zhaoce50.smartdocserver.common.Result;
import com.zhaoce50.smartdocserver.dto.LoginDTO;
import com.zhaoce50.smartdocserver.dto.RegisterDTO;
import com.zhaoce50.smartdocserver.entity.User;
import com.zhaoce50.smartdocserver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
* 认证与用户大堂接待前台
*/
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    //1.用户注册接口
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功！");
    }
    //2.用户登录接口
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto){
        Map<String, Object> data = userService.login(dto);
        return Result.success(data);
    }
    //3.获取当前登录用户信息（必须带token才能进）
    @GetMapping("/info")
    @SaCheckLogin
    public Result<User> getInfo(){
        User user = userService.getCurrentUser();
        return Result.success(user);
    }
    //4.退出登录接口
    @PostMapping("/logout")
    public Result<String> logout(){
        StpUtil.logout();
        return Result.success("已成功退出登录！");
    }
}
