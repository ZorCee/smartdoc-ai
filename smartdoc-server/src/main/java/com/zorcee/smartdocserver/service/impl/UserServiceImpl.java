package com.zorcee.smartdocserver.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zorcee.smartdocserver.common.BusinessException;
import com.zorcee.smartdocserver.common.ErrorCode;
import com.zorcee.smartdocserver.dto.LoginDTO;
import com.zorcee.smartdocserver.dto.RegisterDTO;
import com.zorcee.smartdocserver.entity.User;
import com.zorcee.smartdocserver.mapper.UserMapper;
import com.zorcee.smartdocserver.service.UserService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
* 用户业务实现类
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public void register(RegisterDTO dto){
        Long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0 ) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }
        //2.将DTO的数据组装映射为持久层实体UIser
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setRole("USER");//默认注册角色为普通用户
        user.setStatus(1);//默认状态为1（启用）

        //3.落库：调用Mybatis-plus的save方法写入MySql
        save(user);
    }
    @Override
    public Map<String, Object> login(LoginDTO dto){
        //1.根据用户名从数据库获取用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_PWD_ERR);
        }
        //2.校验密码是否正确
        if (!user.getPassword().equals(dto.getPassword())){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_PWD_ERR);
        }
        //3.校验账户是否被封禁
        if (user.getStatus()!=1){
            throw new BusinessException(ErrorCode.FORBIDDEN, "该账号已被冻结禁用，请联系管理员");
        }
        //4.调用Sa-Token 一行代码完成登录会话绑定
        StpUtil.login(user.getId());

        //5.组装返回给前端的数据载荷（包含Token和用户基本信息）
        Map<String, Object> result = new HashMap<>();
        result.put("tokenName",StpUtil.getTokenName());
        result.put("tokenValue",StpUtil.getTokenValue());
        result.put("userId",user.getId());
        result.put("username",user.getUsername());
        result.put("nickname",user.getNickname());
        result.put("role",user.getRole());
        return result;
    }
    @Override
    public User getCurrentUser(){
        //1.从当前请求凭证解析出用户id
        long userId = StpUtil.getLoginIdAsLong();

        //2.查库返回
        User user = getById(userId);
        if (user == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }
}
