package com.zorcee.smartdocserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
* 创建知识库入参DTO（防越权设计：不暴露ownerId）
*/
@Data
public class CreateKBDTO {
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 50,message = "知识库名称不能超过50个字符")
    private String name;

    @Size(max = 200,message = "描述信息不能超过200个字符")
    private String description;

    //是否公开：0-私有（仅自己可见），1-公开（团队可见），默认为0
    private Integer isPublic = 0;
}
