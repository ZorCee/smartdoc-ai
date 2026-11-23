package com.zhaoce50.smartdocserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoce50.smartdocserver.dto.LoginDTO;
import com.zhaoce50.smartdocserver.dto.RegisterDTO;
import com.zhaoce50.smartdocserver.entity.User;

import java.util.Map;
import java.util.Objects;

/*用户业务层接口
继承 IService<User> 获得 MyBatis-Plus 提供的顶级 Service 增强能力*/
public interface UserService extends IService<User> {
    /*
    * 1.用户注册
    * @param dto 注册入参
    */
    void register(RegisterDTO dto);

    /*
    * 2.用户登录
    * @param dto登录入参
    * @return 包含token、用户昵称、角色等的Map集合
    */
    Map<String, Object> login(LoginDTO dto);

    /*
    * 3.获取当前登录人的详细信息
    * @return当前登录用户实体
    */
    User getCurrentUser();
}
