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
* 知识库文档实体类（对应kb_document表）
*/
@Data
@TableName("kb_document")
public class Document implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long kbId; //所属知识库ID
    private String name; //原始文件名
    private String filePath; //物理保存路径
    private Long fileSize; //文件字节大小
    private String fileType; //文件类型后缀
    private Integer status; //状态：0-已上传，1-解析中，2-向量化中，3-已就绪，4-失败
    private Integer wordCount; //提取出的纯文字字数；
    private String errorMsg; //失败时的错误原因

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
