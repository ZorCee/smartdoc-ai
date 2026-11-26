package com.zorcee.smartdocserver.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/*
* 用户实体类（对应数据库 sys_user表）
*/
@Data
@TableName("sys_user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO) //声明主键，自增
    private Long id;
    private String username;
    @JsonIgnore //关键安全点，返回json时忽略该字段，绝不把密码泄露给前端
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String role; //角色标识：ADMIN/USER
    private Integer status;//状态：1-启用 0-禁用
    @TableLogic //逻辑删除标识：0-未删 1-已删
    @JsonIgnore
    private  Integer isDeleted;
    @JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss")//格式化日期输出
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss")
    private LocalDateTime updateTime;
}



