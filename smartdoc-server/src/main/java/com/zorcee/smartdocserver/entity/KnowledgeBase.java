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
import java.util.regex.Pattern;

/*
* 知识库实体类（对应kb_knowledge_base表）
*/
@Data
@TableName("kb_knowledge_base")
public class KnowledgeBase implements Serializable {
    @TableId(type = IdType.AUTO)//主键自增
    private Long id;
    private String name;//知识库名称
    private String description;//详细描述
    private String avatar;//封面图标
    private Long ownerId;//创建人ID(绑定用户，实现权限隔离)
    private Integer isPublic;//是否公开：0-私有 1-公开
    private String embeddingModel;//向量模型（默认text-embedding-v3）

    @TableLogic //逻辑删除
    @JsonIgnore
    private Integer isDeleted;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
