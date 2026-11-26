package com.zorcee.smartdocserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
* 文档文本切片实体类
*/
@Data
@TableName("kb_document_chunk")
public class DocumentChunk implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long kbId; //所属知识库ID
    private Long docId; //所属文档ID
    private Integer chunkIndex; //切片顺序号（0，1，2.。。。）
    private String content; //切片文本内容
    private Integer charCount; //切片字符数
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer status; // 切片状态: 1-启用检索, 0-禁用隔离
}
