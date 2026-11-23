package com.zhaoce50.smartdocserver.common;

import java.io.Serializable;
import lombok.Data;
/*
* 统一API响应包装类
* @param<T>响应数据的泛型类型
*/
@Data
public class Result<T> implements Serializable {
    private Integer code; //业务状态码：200-成功，500-业务失败，401-未登录，403-无权限
    private  String message; //业务提示语
    private  T data; //泛型数据荷载
    private long timestamp; //响应时间戳

    public Result(){
        this.timestamp = System.currentTimeMillis();
    }
    //快捷成功方法（无返回数据）
    public static <T> Result<T> success() {
        return success(null);
    }
    //快捷成功方法（带返回数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    //快捷失败方法（默认500业务错误）
    public static <T> Result<T> error(String message) {
        return  error(500, message);
    }
    //自定义状态码失败方法（如 401，403，400）
    public static <T> Result<T> error(Integer code, String message){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
