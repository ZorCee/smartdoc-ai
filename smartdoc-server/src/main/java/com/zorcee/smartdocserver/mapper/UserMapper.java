package com.zorcee.smartdocserver.mapper;


/*用户表数据访问层 (Mapper)
继承 BaseMapper<User> 即可直接获得单表全套 CRUD 能力*/


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.zorcee.smartdocserver.entity.User;
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
